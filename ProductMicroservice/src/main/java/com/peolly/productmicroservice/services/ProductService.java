package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.dto.CreateProductRequest;
import com.peolly.productmicroservice.dto.ProductDto;
import com.peolly.productmicroservice.dto.ProductMapper;
import com.peolly.productmicroservice.dto.UpdateProductRequest;
import com.peolly.productmicroservice.exceptions.ProductNotFoundException;
import com.peolly.productmicroservice.kafka.ProductKafkaProducer;
import com.peolly.productmicroservice.models.*;
import com.peolly.productmicroservice.repositories.ProductRepository;
import com.peolly.productmicroservice.repositories.ReservedProductRepository;
import com.peolly.productmicroservice.util.BatchSizeCalculator;
import com.peolly.productmicroservice.util.ProductDataValidator;
import com.peolly.productmicroservice.util.ProductFileProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    @Value("${empty.product.filename}")
    private String emptyProductFile;

    private final ProductRepository productRepository;
    private final ProductDataValidator productDataValidator;
    private final ProductMapper productMapper;
    private final ProductFileProcessor productFileProcessor;
    private final ProductKafkaProducer productKafkaProducer;
    private final BatchSizeCalculator batchSizeCalculator;

    /**
     * Finds a product by its ID.
     *
     * @param id the product ID.
     * @return an Optional containing the found product, or an empty Optional if the product is not found.
     */
    @Cacheable(value = "product", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        return productRepository.findProductById(id);
    }

    /**
     * Validates a list of products from a CSV file and performs the appropriate actions
     * based on validation errors.
     *
     * @param file  the CSV file containing products.
     * @param email the email to send the validation error report (if any).
     */
    @Transactional
    public void validateProducts(MultipartFile file, String email) {
        List<ProductCsvRepresentation> representations = productFileProcessor.parseCsv(file);
        List<ProductValidationSummary> validationErrorReports = productDataValidator.getProductsValidationResult(representations);

        boolean hasErrors = false;

        for (ProductValidationSummary pvs : validationErrorReports) {
            if (!pvs.getValidationErrors().isEmpty() || !pvs.getDuplicateErrors().isEmpty()) {
                hasErrors = true;
                break;
            }
        }

        if (hasErrors) {
            createCsvWithValidationErrors(validationErrorReports, email);
        } else {
            saveProducts(representations);
        }
    }

    /**
     * Saves a list of products to the database in batches.
     *
     * @param productsCsv the list of product representations from CSV.
     */
    @Transactional
    public void saveProducts(List<ProductCsvRepresentation> productsCsv) {
        int batchSize = batchSizeCalculator.getBatchSize();
        for (int i = 0; i < productsCsv.size(); i += batchSize) {
            int end = Math.min(i + batchSize, productsCsv.size());
            List<Product> batch = productsCsv.subList(i, end).stream()
                    .map(this::convertCsvRepresentationToProduct)
                    .toList();
            productRepository.saveAll(batch);
            productRepository.flush();
        }
    }

    /**
     * Validates whether a new product can be created by checking for duplicate names and descriptions.
     *
     * @param createProductRequest the product creation request data.
     * @return true if the product can be created, otherwise false.
     */
    @Transactional
    public boolean validateProduct(CreateProductRequest createProductRequest) {
        ProductDto productDto = productMapper.toDto(createProductRequest);
        Product product = convertDtoToProduct(productDto);

        boolean isNameDuplicate = getExistingProductNames().contains(product.getName());
        boolean isDescriptionDuplicate = getExistingProductDescriptions().contains(product.getDescription());

        if (isNameDuplicate || isDescriptionDuplicate) {
            return false;
        }
        saveProduct(product, createProductRequest.getReportToEmail());
        return true;
    }

    /**
     * Saves a product to the database and sends a product creation event to Kafka.
     *
     * @param product the product to save.
     * @param email   the email to notify about the product creation.
     */
    @Transactional
    public void saveProduct(Product product, String email) {
        productRepository.save(product);
        productKafkaProducer.sendProductCreated(product.getName(), email);
    }

    /**
     * Validates whether a product update is possible by checking for duplicate names and descriptions.
     *
     * @param productId          the ID of the product.
     * @param updateProductRequest the new product update request data.
     * @return true if the update is allowed, otherwise false.
     */
    @Transactional
    public boolean validateUpdatedProduct(Long productId, UpdateProductRequest updateProductRequest) {
        ProductDto productDto = productMapper.toDto(updateProductRequest);
        Product updatedProduct = convertDtoToProduct(productDto);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));

        boolean isNameDuplicate = getExistingProductNames().stream()
                .anyMatch(name -> name.equals(updatedProduct.getName()) && !name.equals(existingProduct.getName()));

        boolean isDescriptionDuplicate = getExistingProductDescriptions().stream()
                .anyMatch(description -> description.equals(updatedProduct.getDescription()) && !description.equals(existingProduct.getDescription()));

        return !isNameDuplicate && !isDescriptionDuplicate;
    }

    /**
     * Updates an existing product's data and clears the cache.
     *
     * @param id      the ID of the product.
     * @param newData the new data for the update.
     * @return the updated product object.
     */
    @Transactional
    @CacheEvict(value = "product", key = "#id")
    public Product updateProduct(Long id, UpdateProductRequest newData) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
        productMapper.updateProductData(newData, existingProduct);
        productRepository.saveAndFlush(existingProduct);
        return existingProduct;
    }

    /**
     * Generates a CSV file containing validation errors and sends it via email.
     *
     * @param validationSummary the list of validation errors.
     * @param email             the recipient's email.
     */
    private void createCsvWithValidationErrors(List<ProductValidationSummary> validationSummary, String email) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8))) {
            for (ProductValidationSummary summary : validationSummary) {
                if (summary.getValidationErrors() != null) {
                    for (ProductValidationReport report : summary.getValidationErrors()) {
                        for (String errorMessage : report.getErrorMessages()) {
                            writer.printf("%s - %s%n",
                                    report.getIncorrectData(),
                                    errorMessage);
                        }
                    }
                }

                if (summary.getDuplicateErrors() != null) {
                    for (ProductsDuplicateReport duplicate : summary.getDuplicateErrors()) {
                        writer.printf("%s %s%n",
                                duplicate.getDuplicateData(),
                                duplicate.getErrorMessages());
                    }
                }
            }
        }
        sendValidationReportToEmail(byteArrayOutputStream.toByteArray(), email);
    }

    /**
     * Sends a CSV file with validation error reports to the specified email.
     *
     * @param csvData the CSV data as a byte array.
     * @param email   the recipient's email.
     */
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("http://localhost:8010/s3/upload", requestEntity, String.class);
    }

    /**
     * Converts a product DTO to a Product entity with predefined values.
     *
     * @param productDto the product DTO.
     * @return the Product entity.
     */
    private Product convertDtoToProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        product.setImage(emptyProductFile);
        product.setStorageAmount(0);
        product.setEvaluation(0.0);
        return product;
    }

    /**
     * Converts a CSV product representation into a Product entity with predefined values.
     *
     * @param csvRepresentation the product representation from CSV.
     * @return the Product entity.
     */
    private Product convertCsvRepresentationToProduct(ProductCsvRepresentation csvRepresentation) {
        Product product = productMapper.toDto(csvRepresentation);
        product.setImage(emptyProductFile);
        product.setStorageAmount(100);
        product.setEvaluation(0.0);
        return product;
    }

    /**
     * Retrieves a set of existing product descriptions.
     *
     * @return a set of product descriptions.
     */
    @Transactional(readOnly = true)
    public Set<String> getExistingProductDescriptions() {
        return productRepository.findAllProductDescriptions();
    }

    /**
     * Retrieves a set of existing product names.
     *
     * @return a set of product names.
     */
    @Transactional(readOnly = true)
    public Set<String> getExistingProductNames() {
        return productRepository.findAllProductNames();
    }

    @Transactional
    public void decreaseProductStorageAmount(Long productId, int amount) {
        productRepository.decreaseProductStorageAmount(productId, amount);
    }
}
