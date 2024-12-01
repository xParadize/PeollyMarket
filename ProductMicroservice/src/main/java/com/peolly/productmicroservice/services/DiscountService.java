package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.models.Discount;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.repositories.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;

    @Transactional
    public void deleteAllDiscounts() {
        discountRepository.deleteAll();
    }

    @Transactional
    public void saveDiscount(Discount discountToSave) {
        discountRepository.save(discountToSave);
    }

    @Transactional
    public void deleteAllByProduct(Product product) {
        discountRepository.deleteAllByProduct(product);
    }
}
