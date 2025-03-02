package com.peolly.productmicroservice.controllers;

import com.peolly.productmicroservice.dto.ApiResponse;
import com.peolly.productmicroservice.exceptions.EmptyProductStorageAmountException;
import com.peolly.productmicroservice.services.ReservedProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("store/api/v1")
@RequiredArgsConstructor
public class ProductApiController {
    private final ReservedProductService reservedProductService;

    @PostMapping("/reserve-products")
    public ResponseEntity<?> reserveProduct(List<Long> productIds, UUID userId) {
        try {
            reservedProductService.processOrder(productIds, userId);
        } catch (EmptyProductStorageAmountException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
