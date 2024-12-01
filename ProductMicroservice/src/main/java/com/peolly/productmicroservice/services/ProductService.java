package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.repositories.ProductRepository;
import com.peolly.productmicroservice.util.ProductDataValidator;
import com.peolly.utilservice.events.SendCreateProductEvent;
import com.peolly.utilservice.events.SendProductDataHaveProblemsEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${empty.product.filename}")
    private String emptyProductFile;

    private final ProductRepository productRepository;
    private final ProductDataValidator productDataValidator;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, SendProductDataHaveProblemsEvent> sendIsProductDescriptionRepeatEvent;

    @Transactional
    @KafkaListener(topics = "send-create-product", groupId = "org-deli-queuing-company")
    public void createProduct(SendCreateProductEvent createProductEvent) throws ExecutionException, InterruptedException {

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

    private void sendProductDataHaveProblemsEvent(List<String> invalidFields) throws ExecutionException, InterruptedException {
        SendProductDataHaveProblemsEvent event = new SendProductDataHaveProblemsEvent(invalidFields);
        ProducerRecord<String, SendProductDataHaveProblemsEvent> record = new ProducerRecord<>(
                "send-product-duplicate-detected",
                event
        );

        SendResult<String, SendProductDataHaveProblemsEvent> result = sendIsProductDescriptionRepeatEvent
                .send(record).get();

        LOGGER.info("Sent event: {}", result);
    }

//    @Transactional(readOnly = true)
//    public List<ProductDTO> productsPagination(int page, int productsPerPage) {
//        List<Product> products = productsRepository.findAll(PageRequest.of(page, productsPerPage)).getContent();
//        if (products.isEmpty()) throw new ResourceNotFoundException();
//
//        List<ProductDTO> dtosToReturn = new ArrayList<>();
//
//        for (Product p : products) {
//            ProductDTO dto = convertProductToDto(p);
//            dtosToReturn.add(dto);
//        }
//        return dtosToReturn;
//    }
//
//    @Transactional(readOnly = true)
//    public List<ProductDTO> findProductsByOrganizationNameWithPagination(String organizationName, Integer page, Integer productsPerPage) {
//        int offset = page * productsPerPage;
//        Organization organization = organizationService.findOrganizationByName(organizationName);
//        if (organization == null) throw new OrganizationNotFoundException();
//
//        List<Product> products = productsRepository.findProductsByOrganizationNameWithPagination(organization.getId(), offset, productsPerPage);
//        if (products.isEmpty()) throw new OrganizationHasNoProductsException();
//
//        List<ProductDTO> productsToReturn = new ArrayList<>();
//        for (Product p : products) {
//            productsToReturn.add(convertProductToDto(p));
//        }
//        return productsToReturn;
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
//    private ProductDTO convertProductToDto(Product productToConvert) {
//        OrganizationDTO orgDto = OrganizationDTO.builder()
//                .name(productToConvert.getOrganization().getName())
//                .description(productToConvert.getOrganization().getDescription())
//                .build();
//
//        ProductDTO dtoToShow = ProductDTO.builder()
//                .name(productToConvert.getName())
//                .description(productToConvert.getDescription())
//                .organizationDTO(orgDto)
//                .price(productToConvert.getPrice())
//                .build();
//        return dtoToShow;
//    }
}
