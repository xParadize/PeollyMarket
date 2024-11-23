package com.peolly.securityserver.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Service
public class KafkaListenerFutureWaiter {
    private CompletableFuture<Boolean> paymentMethodFuture;
    private CompletableFuture<List<String>> allPaymentMethodsFuture;
}