package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.dto.ProductDto;
import com.peolly.productmicroservice.dto.ProductMapper;
import com.peolly.productmicroservice.exceptions.CompanyHasNoProductsException;
import com.peolly.productmicroservice.exceptions.IncorrectSearchPath;
import com.peolly.productmicroservice.kafka.ProductKafkaProducer;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.repositories.ProductRepository;
import com.peolly.productmicroservice.util.ProductDataValidator;
import com.peolly.schemaregistry.CreateProductEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ProductService {
    @Value("${empty.product.filename}")
    private String emptyProductFile;

    private final ProductRepository productRepository;
    private final ProductDataValidator productDataValidator;
    private final ProductKafkaProducer productKafkaProducer;
    private final ProductMapper productMapper;

    private final List<Product> productBatch = Collections.synchronizedList(new ArrayList<>());
    private final int BATCH_SIZE = 100;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Transactional
    @KafkaListener(topics = "create-product-requests", groupId = "org-deli-queuing-company")
    public void consumeCreateProductRequests(ConsumerRecord<String, GenericRecord> message) {
        SpecificData specificData = new SpecificData();

        CreateProductEvent event = (CreateProductEvent) specificData.deepCopy(
                CreateProductEvent.SCHEMA$, message.value());

        List<String> invalidFields = productDataValidator.validateProductData(
                event.getName().toString(), event.getDescription().toString()
        );

        if (!invalidFields.isEmpty()) {
//            productKafkaProducer (invalidFields);
            System.out.println("Errors are here!!!");
        } else {
            saveProduct(event);
        }
    }

    @Transactional
    public void saveProduct(CreateProductEvent event) {
        Product product = Product.builder()
                .name(event.getName().toString())
                .description(event.getDescription().toString())
                .image(emptyProductFile)
                .companyId(event.getCompanyId())
                .price(event.getPrice())
                .storageAmount(0)
                .evaluation(0.0)
                .build();
        productRepository.save(product);

        synchronized (productBatch) {
            productBatch.add(product);
            if (productBatch.size() >= BATCH_SIZE) {
                processBatch();
            }
        }
    }

    private void processBatch() {
        List<Product> batchToSave;
        synchronized (productBatch) {
            if (productBatch.isEmpty()) return;
            batchToSave = new ArrayList<>(productBatch);
            productBatch.clear();
        }
        executorService.submit(() -> saveProductsInBatch(batchToSave));
    }

    private void saveProductsInBatch(List<Product> batchToSave) {
        try {
            productRepository.saveAll(batchToSave);
            productRepository.flush();
        } catch (Exception e) {
            System.err.println("Error saving product batch: " + e.getMessage());
        }
    }

    @PostConstruct
    public void initializeBatchTimer() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    processBatch();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Transactional(readOnly = true)
    public List<ProductDto> productsPagination(int page, int productsPerPage) {
        List<Product> products = productRepository.findAll(PageRequest.of(page, productsPerPage)).getContent();
        if (products.isEmpty()) throw new IncorrectSearchPath();

        List<ProductDto> productsToReturn = products.stream()
                .map(this::convertProductToDto)
                .toList();
        return productsToReturn;
    }

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

    private int calculatePageOffset(int page, int productsPerPage) {
        return page * productsPerPage;
    }

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

    private void validateCompanyHasProducts(Long companyId) {
        long totalProductsCount = productRepository.countProductsByCompanyId(companyId);
        if (totalProductsCount == 0) {
            throw new CompanyHasNoProductsException();
        }
    }

    private List<Product> fetchProductsForPage(Long companyId, int offset, int productsPerPage) {
        return productRepository.findProductsByCompanyIdWithPagination(companyId, offset, productsPerPage);
    }

    private void validatePageHasProducts(List<Product> products) {
        if (products.isEmpty()) {
            throw new IncorrectSearchPath();
        }
    }

    private List<ProductDto> convertProductsToDtos(List<Product> products) {
        return products.stream()
                .map(this::convertProductToDto)
                .toList();
    }

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
    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public Product findProductById(Long id) {
        return productRepository.findProductById(id).orElse(null);
    }

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
    private ProductDto convertProductToDto(Product productToConvert) {
        return productMapper.toDto(productToConvert);
    }
}
