package com.peolly.securityserver.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Service
public class SecurityKafkaListenerFutureWaiter {
    private CompletableFuture<Boolean> wasPaymentMethodAddedFuture;
    private CompletableFuture<List<String>> allPaymentMethodsFuture;
    private CompletableFuture<Boolean> wasPaymentMethodDeletedFuture;
}