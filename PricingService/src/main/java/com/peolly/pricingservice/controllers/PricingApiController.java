package com.peolly.pricingservice.controllers;

import com.peolly.pricingservice.dto.ApiResponse;
import com.peolly.pricingservice.external.PricesRefreshRequest;
import com.peolly.pricingservice.external.PricesRefreshResponse;
import com.peolly.pricingservice.services.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class PricingApiController {
    private final PriceService priceService;

    @PostMapping("/get-price")
    public ResponseEntity<?> updatePrices(@RequestBody List<PricesRefreshRequest> priceRequests) {
        List<Long> itemIds = priceRequests.stream()
                .map(PricesRefreshRequest::itemId)
                .collect(Collectors.toList());
        List<PricesRefreshResponse> updatedPrices = priceService.getPricesWithDiscount(itemIds);
        if (updatedPrices.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Price not found"), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(updatedPrices);
    }
}
