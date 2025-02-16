package com.peolly.ordermicroservice.util;

import com.peolly.ordermicroservice.exceptions.ProductNotFoundException;
import com.peolly.ordermicroservice.external.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CartRestService {
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches product information from an external store service.
     *
     * @param productId the unique identifier of the product.
     * @return ProductDto object containing product details.
     * @throws ProductNotFoundException if the product is not found or the request fails.
     */
    public ProductDto getProductInfo(Long productId) {
        String url = "http://localhost:8003/store/product/" + productId;

        ResponseEntity<ProductDto> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(url, ProductDto.class);
        } catch (Exception e) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        return responseEntity.getBody();
    }
}

