package com.peolly.productmicroservice.services;

import com.peolly.productmicroservice.exceptions.EmptyProductStorageAmountException;
import com.peolly.productmicroservice.models.Product;
import com.peolly.productmicroservice.models.ReservedProduct;
import com.peolly.productmicroservice.repositories.ReservedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservedProductService {
    private final ProductService productService;
    private final ReservedProductRepository reservedProductRepository;

    @Transactional
    public void processOrder(List<Long> productIds, UUID userId) {
        // Резервируем товар на складе
        // Уменьшаем кол-во товара на складе

        for (Long productId : productIds) {
            Product product = productService.findProductById(productId).orElse(null);
            if (product.getStorageAmount() > 0) {
                reserveProduct(productId, userId);
            } else {
                throw new EmptyProductStorageAmountException("Please Refresh Cart Info.");
            }
        }
    }

    public void reserveProduct(Long productId, UUID userId) {
        ReservedProduct reservedProduct = ReservedProduct.builder()
                .productId(productId)
                .amount(1)
                .reservedBy(userId)
                .reservedAt(LocalDateTime.now())
                .build();
        reservedProductRepository.save(reservedProduct);
        productService.decreaseProductStorageAmount(productId, 1);
    }
}
