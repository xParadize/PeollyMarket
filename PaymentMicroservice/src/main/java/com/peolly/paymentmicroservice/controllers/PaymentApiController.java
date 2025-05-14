package com.peolly.paymentmicroservice.controllers;

import com.peolly.paymentmicroservice.dto.ApiResponse;
import com.peolly.paymentmicroservice.dto.PaymentRequestDto;
import com.peolly.paymentmicroservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment/api/v1")
@RequiredArgsConstructor
public class PaymentApiController {
    private final PaymentService paymentService;

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequestDto paymentRequestDto) {
        if (!paymentService.isCardValidForPayment(
                paymentRequestDto.cardNumber(), paymentRequestDto.userId(),
                paymentRequestDto.totalCost())) {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid card or insufficient funds"), HttpStatus.BAD_REQUEST);
        }

        paymentService.performPayment(paymentRequestDto);

        return ResponseEntity.ok("Payment Successful.");
    }



}
