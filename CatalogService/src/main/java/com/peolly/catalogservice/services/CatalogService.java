package com.peolly.catalogservice.services;

import com.peolly.catalogservice.client.StorageServiceClient;
import com.peolly.catalogservice.dto.*;
import com.peolly.catalogservice.exceptions.InsufficientStockException;
import com.peolly.catalogservice.exceptions.ItemNotFoundException;
import com.peolly.catalogservice.exceptions.PriceNotFoundException;
import com.peolly.catalogservice.external.PricesRefreshRequest;
import com.peolly.catalogservice.external.PricesRefreshResponse;
import com.peolly.catalogservice.kafka.CatalogKafkaProducer;
import com.peolly.catalogservice.mappers.CatalogItemMapper;
import com.peolly.catalogservice.models.CatalogItem;
import com.peolly.catalogservice.models.Category;
import com.peolly.catalogservice.repositories.CatalogRepository;
import com.peolly.catalogservice.util.BatchSizeCalculator;
import com.peolly.catalogservice.util.ItemDataValidator;
import com.peolly.catalogservice.util.ItemFileProcessor;
import com.peolly.schemaregistry.FileCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final StorageServiceClient storageServiceClient;
    private final CatalogRepository catalogRepository;
    private final CatalogItemMapper catalogItemMapper;
    private final CategoryService categoryService;
    private final CatalogKafkaProducer catalogKafkaProducer;
    private final ItemFileProcessor itemFileProcessor;
    private final ItemDataValidator itemDataValidator;
    private final BatchSizeCalculator batchSizeCalculator;

    private final RestClient restClient = RestClient.create();

    @Cacheable(value = "item", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public ItemDto findItemById(Long id) {
        Optional<CatalogItem> optItemInfo = catalogRepository.findCatalogItemById(id);
        CatalogItem itemInfo = optItemInfo.orElseThrow(() -> new ItemNotFoundException("Item with this ID not found."));

        Integer itemQuantity = getStockQuantity(id);
        Double itemPrice = getItemPrice(id);

        ItemDto itemDto = new ItemDto(
                itemInfo.getId(),
                itemInfo.getName(),
                itemInfo.getDescription(),
                itemInfo.getImage(),
                itemPrice,
                itemQuantity
        );
        return itemDto;
    }

    private Double getItemPrice(Long id) {
        PricesRefreshRequest request = new PricesRefreshRequest(id, 1, 0.0);
        List<PricesRefreshRequest> requestList = List.of(request);

        List<PricesRefreshResponse> response = restClient.post()
                .uri("http://localhost:8032/api/v1/pricing/items/prices")
                .body(requestList)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request_, response_) -> {
                    throw new PriceNotFoundException("Price not found.");
                }))
                .body(new ParameterizedTypeReference<>() {});

        if (response.isEmpty()) throw new PriceNotFoundException("Price not found.");
        return response.get(0).updatedPrice();
    }



    private Integer getStockQuantity(Long id) {
        Integer itemQuantity = restClient.get()
                .uri("http://localhost:8031/api/v1/storage/items/{id}/quantity", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    throw new InsufficientStockException("Item not found or is out of the stock.");
                }))
                .body(Integer.class);
        return itemQuantity;
    }

    @Transactional
    public boolean isItemDuplicate(CreateProductRequest createProductRequest) {
        ItemDuplicateRequest validationRequest = new ItemDuplicateRequest(
                createProductRequest.getName(),
                createProductRequest.getDescription()
        );

        List<Boolean> result = storageServiceClient.isDuplicate(List.of(validationRequest));
        return result.get(0);
    }

    @Transactional
    public void validateProducts(MultipartFile file, String email) {
        List<ItemCsvRepresentation> representations = itemFileProcessor.parseCsv(file);
        List<ItemValidationSummary> validationErrorReports = itemDataValidator.getItemsValidationResult(representations);

        boolean hasErrors = false;

        for (ItemValidationSummary pvs : validationErrorReports) {
            if (!pvs.getValidationErrors().isEmpty() || !pvs.getDuplicateErrors().isEmpty()) {
                hasErrors = true;
                break;
            }
        }

        if (hasErrors) {
            createCsvWithValidationErrors(validationErrorReports, email);
        } else {
            saveCatalogItems(representations, email);
        }
    }

    private void createCsvWithValidationErrors(List<ItemValidationSummary> validationSummary, String email) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))) {
            for (ItemValidationSummary summary : validationSummary) {
                if (summary.getValidationErrors() != null) {
                    for (ItemValidationReport report : summary.getValidationErrors()) {
                        for (String errorMessage : report.getErrorMessages()) {
                            writer.printf("%s - %s%n",
                                    report.getIncorrectData(),
                                    errorMessage);
                        }
                    }
                }

                if (summary.getDuplicateErrors() != null) {
                    for (ItemDuplicateReport duplicate : summary.getDuplicateErrors()) {
                        writer.printf("%s %s%n",
                                duplicate.getDuplicateData(),
                                duplicate.getErrorMessages());
                    }
                }
            }
        }
        sendValidationReportToEmail(byteArrayOutputStream.toByteArray(), email);
    }

    private void sendValidationReportToEmail(byte[] csvData, String email) {
        RestTemplate restTemplate = new RestTemplate();

        ByteArrayResource byteArrayResource = new ByteArrayResource(csvData) {
            @Override
            public String getFilename() {
                long time = System.currentTimeMillis();
                return time + "_validation_errors.csv";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", byteArrayResource);
        body.add("email", email);
        body.add("fileCategory", FileCategory.PRODUCT_VALIDATION_REPORT.name());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("http://localhost:8010/api/v1/s3/files", requestEntity, String.class);
    }

    @Transactional
    public void saveCatalogItem(CreateProductRequest createProductRequest, String creatorEmail) {
        Category category = categoryService.findCategoryById(createProductRequest.getCategoryId());

        CatalogItem catalogItem = catalogItemMapper.toEntity(createProductRequest);
        catalogItem.setCategory(category);

        catalogRepository.save(catalogItem);

        catalogKafkaProducer.sendCreateItemEvent(createProductRequest, creatorEmail, catalogItem.getId());
    }

    @Transactional
    public void saveCatalogItems(List<ItemCsvRepresentation> itemCSV, String creatorEmail) {
        int batchSize = batchSizeCalculator.getBatchSize();
        for (int i = 0; i < itemCSV.size(); i += batchSize) {
            int end = Math.min(i + batchSize, itemCSV.size());

            List<ItemCsvRepresentation> batchInput = itemCSV.subList(i, end);
            List<CatalogItem> batchEntities = new ArrayList<>();

            for (ItemCsvRepresentation csv : batchInput) {
                CatalogItem catalogItem = catalogItemMapper.toDto(csv);
                Category category = categoryService.findCategoryById(csv.getCategoryId());
                catalogItem.setCategory(category);

                batchEntities.add(catalogItem);
            }

            catalogRepository.saveAll(batchEntities);
            catalogRepository.flush();

            catalogKafkaProducer.sendCreateItemsEvent(batchInput, creatorEmail, batchEntities);
        }
    }
}
