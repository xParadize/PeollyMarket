package com.peolly.catalogservice.client;

import com.peolly.catalogservice.dto.ItemDuplicateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "storage-service", 
    url = "http://localhost:8031"
)
public interface StorageServiceClient {
    @PostMapping("/api/v1/storage/check-duplicate")
    Boolean isDuplicate(@RequestBody ItemDuplicateRequest request);
}