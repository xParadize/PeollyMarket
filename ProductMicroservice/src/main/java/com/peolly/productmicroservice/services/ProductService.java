package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.dto.CreateProductRequest;
import com.peolly.productmicroservice.dto.ProductDto;
import com.peolly.productmicroservice.dto.ProductMapper;
import com.peolly.productmicroservice.dto.UpdateProductRequest;
import com.peolly.productmicroservice.exceptions.ProductNotFoundException;
import com.peolly.productmicroservice.kafka.ProductKafkaProducer;
import com.peolly.productmicroservice.models.*;
import com.peolly.productmicroservice.repositories.ProductRepository;
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

    @Cacheable(value = "product", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long id) {
        return productRepository.findProductById(id);
    }

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

    @Transactional
    public void saveProducts(List<ProductCsvRepresentation> productsCsv) {
        for (ProductCsvRepresentation p : productsCsv) {
            System.out.println(p.toCsvString());
        }

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

    @Transactional
    public void saveProduct(Product product, String email) {
        productRepository.save(product);
        productKafkaProducer.sendProductCreated(product.getName(), email);
    }

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

    @Transactional
    @CacheEvict(value = "product", key = "#id")
    public Product updateProduct(Long id, UpdateProductRequest newData) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
        productMapper.updateProductData(newData, existingProduct);
        productRepository.saveAndFlush(existingProduct);
        return existingProduct;
    }

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

//    @Transactional(readOnly = true)
//    public List<ProductDto> productsPagination(int page, int productsPerPage) {
//        List<Product> products = productRepository.findAll(PageRequest.of(page, productsPerPage)).getContent();
//        if (products.isEmpty()) throw new IncorrectSearchPath();
//
//        List<ProductDto> productsToReturn = products.stream()
//                .map(this::convertProductToDto)
//                .toList();
//        return productsToReturn;
//    }

//    @Transactional(readOnly = true)
//    public List<ProductDto> findProductsByCompanyIdWithPagination(Long companyId, int page, int productsPerPage)
//            throws ExecutionException, InterruptedException {
//        int offset = calculatePageOffset(page, productsPerPage);
//
//        GetCompanyByIdResponseEvent companyData = fetchCompanyData(companyId);
//        validateCompanyHasProducts(companyData.companyId());
//
//        List<Product> products = fetchProductsForPage(companyData.companyId(), offset, productsPerPage);
//        validatePageHasProducts(products);
//
//        return convertProductsToDtos(products);
//    }

//    private int calculatePageOffset(int page, int productsPerPage) {
//        return page * productsPerPage;
//    }

//    private GetCompanyByIdResponseEvent fetchCompanyData(Long companyId) throws ExecutionException, InterruptedException {
//        productKafkaProducer.sendGetCompanyById(companyId);
//        CompletableFuture<GetCompanyByIdResponseEvent> future = new CompletableFuture<>();
//        productKafkaListenerFutureWaiter.setCompanyIdResponse(future);
//
//        GetCompanyByIdResponseEvent companyData = future.get();
//        if (companyData.companyName() == null) {
//            throw new CompanyNotFoundException();
//        }
//        return companyData;
//    }

//    private void validateCompanyHasProducts(Long companyId) {
//        long totalProductsCount = productRepository.countProductsByCompanyId(companyId);
//        if (totalProductsCount == 0) {
//            throw new CompanyHasNoProductsException();
//        }
//    }
//
//    private List<Product> fetchProductsForPage(Long companyId, int offset, int productsPerPage) {
//        return productRepository.findProductsByCompanyIdWithPagination(companyId, offset, productsPerPage);
//    }
//
//    private void validatePageHasProducts(List<Product> products) {
//        if (products.isEmpty()) {
//            throw new IncorrectSearchPath();
//        }
//    }



//    @Transactional
//    @KafkaListener(topics = "send-get-company-by-id-response", groupId = "org-deli-queuing-company")
//    public void consumeGetCompanyByIdResponseEvent(GetCompanyByIdResponseEvent event) {
//        CompletableFuture<GetCompanyByIdResponseEvent> future = productKafkaListenerFutureWaiter.getCompanyIdResponse();
//        future.complete(event);
//    }
//
//    @Transactional(readOnly = true)
//    public ProductDTO getProductInfo(Long productId) {
//        Optional<Product> optionalProduct = productsRepository.findById(productId);
//
//        if (optionalProduct.isPresent()) {
//            Product productToConvert = optionalProduct.get();
//            return convertProductToDto(productToConvert);
//        }
//        return null;
//    }
//


//    @CacheEvict(value = "product", key = "#product.id")
//    public void updateProduct(Product product) {
//        System.out.println("Product updated in DB");
//        productRepository.save(product);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Product> getAllProductsWithDiscount() {
//        return productsRepository.findAllProductsWithDiscount();
//    }
//
//    @Transactional
//    public void changeDiscount(Integer discountId, Long productId) {
//        productsRepository.changeDiscount(discountId, productId);
//    }

    private Product convertDtoToProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        product.setImage(emptyProductFile);
        product.setStorageAmount(0);
        product.setEvaluation(0.0);
        return product;
    }

    private Product convertCsvRepresentationToProduct(ProductCsvRepresentation csvRepresentation) {
        Product product = productMapper.toDto(csvRepresentation);
        product.setImage(emptyProductFile);
        product.setStorageAmount(0);
        product.setEvaluation(0.0);
        return product;
    }

    @Transactional(readOnly = true)
    public Set<String> getExistingProductDescriptions() {
        return productRepository.findAllProductDescriptions();
    }

    @Transactional(readOnly = true)
    public Set<String> getExistingProductNames() {
        return productRepository.findAllProductNames();
    }
}
