package com.peolly.catalogservice.services;

import com.peolly.catalogservice.client.StorageServiceClient;
import com.peolly.catalogservice.dto.CreateProductRequest;
import com.peolly.catalogservice.dto.ItemDto;
import com.peolly.catalogservice.dto.ItemDuplicateRequest;
import com.peolly.catalogservice.exceptions.InsufficientStockException;
import com.peolly.catalogservice.exceptions.ItemNotFoundException;
import com.peolly.catalogservice.exceptions.PriceNotFoundException;
import com.peolly.catalogservice.kafka.CatalogKafkaProducer;
import com.peolly.catalogservice.mappers.CatalogItemMapper;
import com.peolly.catalogservice.models.CatalogItem;
import com.peolly.catalogservice.models.Category;
import com.peolly.catalogservice.repositories.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

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
        Double itemPrice = restClient.post()
                .uri("http://localhost:8032/api/v1/pricing/get-price")
                .body(List.of(id))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    throw new PriceNotFoundException("Price not found.");
                }))
                .body(Double.class);
        return itemPrice;
    }

    private Integer getStockQuantity(Long id) {
        Integer itemQuantity = restClient.post()
                .uri("http://localhost:8031/api/v1/storage/get-quantity")
                .body(id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    throw new InsufficientStockException("Item not found or is out of the stock.");
                }))
                .body(Integer.class);
        return itemQuantity;
    }

    @Transactional
    public boolean isProductDuplicate(CreateProductRequest createProductRequest) {
        ItemDuplicateRequest validationRequest = new ItemDuplicateRequest(
                createProductRequest.getName(),
                createProductRequest.getDescription()
        );
        return storageServiceClient.isDuplicate(validationRequest);
    }

    @Transactional
    public void saveCatalogItem(CreateProductRequest createProductRequest, String creatorEmail) {
        Category category = categoryService.findCategoryById(createProductRequest.getCategoryId());

        CatalogItem catalogItem = catalogItemMapper.toEntity(createProductRequest);
        catalogItem.setCategory(category);

        catalogRepository.save(catalogItem);

        catalogKafkaProducer.sendCreateItemEvent(createProductRequest, creatorEmail, catalogItem.getId());
    }
}
