package com.peolly.productmicroservice.kafka;

import com.peolly.utilservice.events.GetCompanyByIdResponseEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Service
public class ProductKafkaListenerFutureWaiter {
    private CompletableFuture<GetCompanyByIdResponseEvent> companyIdResponse;
}