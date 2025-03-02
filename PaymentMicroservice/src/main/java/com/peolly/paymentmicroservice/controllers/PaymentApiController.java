package com.peolly.paymentmicroservice.controllers;

import com.peolly.paymentmicroservice.dto.ApiResponse;
import com.peolly.paymentmicroservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payment/api/v1")
@RequiredArgsConstructor
public class PaymentApiController {
    private final PaymentService paymentService;

    @GetMapping("/process_payment")
    public ResponseEntity<?> pay(String cardNumber, UUID userId, double totalCost, Long orderId) {
        if (!paymentService.isCardValidForPayment(cardNumber, userId, totalCost)) {
            return new ResponseEntity<>(new ApiResponse(false, "Incorrect card information or insufficient money"), HttpStatus.BAD_REQUEST);
        }
        paymentService.performPayment(cardNumber, userId, totalCost, orderId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
