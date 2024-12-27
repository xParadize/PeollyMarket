package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.dto.ProductDto;
import com.peolly.productmicroservice.dto.ProductMapper;
import com.peolly.productmicroservice.exceptions.CompanyHasNoProductsException;
import com.peolly.productmicroservice.exceptions.CompanyNotFoundException;
import com.peolly.productmicroservice.exceptions.IncorrectSearchPath;
import com.peolly.productmicroservice.kafka.ProductKafkaListenerFutureWaiter;
import com.peolly.productmicroservice.kafka.ProductKafkaProducer;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.repositories.ProductRepository;
import com.peolly.productmicroservice.util.ProductDataValidator;
import com.peolly.utilservice.events.CreateProductEvent;
import com.peolly.utilservice.events.GetCompanyByIdResponseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ProductService {
    @Value("${empty.product.filename}")
    private String emptyProductFile;

    private final ProductRepository productRepository;
    private final ProductDataValidator productDataValidator;
    private final ProductKafkaProducer productKafkaProducer;
    private final ProductMapper productMapper;
    private final ProductKafkaListenerFutureWaiter productKafkaListenerFutureWaiter;

    @Transactional
    @KafkaListener(topics = "send-create-product", groupId = "org-deli-queuing-company")
    public void createProduct(CreateProductEvent createProductEvent) {
        List<String> invalidFields = productDataValidator.validateProductData(
                createProductEvent.name(), createProductEvent.description());

        if (!invalidFields.isEmpty()) {
            sendProductDataHaveProblemsEvent(invalidFields);
        } else {
            Product product = Product.builder()
                    .name(createProductEvent.name())
                    .description(createProductEvent.description())
                    .image(emptyProductFile)
                    .companyId(createProductEvent.companyId())
                    .price(createProductEvent.price())
                    .storageAmount(0)
                    .evaluation(0.0)
                    .build();
            productRepository.save(product);
            sendProductDataHaveProblemsEvent(Collections.emptyList());
        }
    }

    private void sendProductDataHaveProblemsEvent(List<String> invalidFields) {
        productKafkaProducer.sendEmailConfirmed(invalidFields);
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

    @Transactional(readOnly = true)
    public List<ProductDto> findProductsByCompanyIdWithPagination(Long companyId, int page, int productsPerPage)
            throws ExecutionException, InterruptedException {
        int offset = calculatePageOffset(page, productsPerPage);

        GetCompanyByIdResponseEvent companyData = fetchCompanyData(companyId);
        validateCompanyHasProducts(companyData.companyId());

        List<Product> products = fetchProductsForPage(companyData.companyId(), offset, productsPerPage);
        validatePageHasProducts(products);

        return convertProductsToDtos(products);
    }

    private int calculatePageOffset(int page, int productsPerPage) {
        return page * productsPerPage;
    }

    private GetCompanyByIdResponseEvent fetchCompanyData(Long companyId) throws ExecutionException, InterruptedException {
        productKafkaProducer.sendGetCompanyById(companyId);
        CompletableFuture<GetCompanyByIdResponseEvent> future = new CompletableFuture<>();
        productKafkaListenerFutureWaiter.setCompanyIdResponse(future);

        GetCompanyByIdResponseEvent companyData = future.get();
        if (companyData.companyName() == null) {
            throw new CompanyNotFoundException();
        }
        return companyData;
    }

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

    @Transactional
    @KafkaListener(topics = "send-get-company-by-id-response", groupId = "org-deli-queuing-company")
    public void consumeGetCompanyByIdResponseEvent(GetCompanyByIdResponseEvent event) {
        CompletableFuture<GetCompanyByIdResponseEvent> future = productKafkaListenerFutureWaiter.getCompanyIdResponse();
        future.complete(event);
    }
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
//    @Transactional(readOnly = true)
//    public Product findProductById(Long productId) {
//        return productsRepository.findById(productId).orElse(null);
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
//
    private ProductDto convertProductToDto(Product productToConvert) {
        return productMapper.toDto(productToConvert);
    }
}
