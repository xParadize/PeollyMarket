package com.peolly.storageservice.controllers;

import com.peolly.storageservice.dto.ItemDuplicateRequest;
import com.peolly.storageservice.services.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/check-duplicate")
    public ResponseEntity<Boolean> isDuplicate(@RequestBody ItemDuplicateRequest request) {
        boolean isDuplicate = storageService.isDuplicate(request.name(), request.description());
        return ResponseEntity.ok(isDuplicate);
    }
}