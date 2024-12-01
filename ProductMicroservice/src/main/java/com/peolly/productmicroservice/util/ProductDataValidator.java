package com.peolly.productmicroservice.util;

import com.peolly.productmicroservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDataValidator {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<String> validateProductData(String name, String description) {
        List<String> invalidFields = new ArrayList<>();
        if (checkProductNameRepeats(name)) {
            invalidFields.add("name");
        }
        if (checkProductDescriptionRepeats(description)) {
            invalidFields.add("description");
        }
        return invalidFields;
    }

    private boolean checkProductNameRepeats(String productName) {
        return productRepository.findProductByName(productName).isPresent();
    }

    private boolean checkProductDescriptionRepeats(String productDescription) {
        return productRepository.findProductByDescription(productDescription).isPresent();
    }
}

