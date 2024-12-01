package com.peolly.companymicroservice.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Service
public class CompanyKafkaListenerFutureWaiter {
    private CompletableFuture<List<String>> invalidProductFields;
}