package com.peolly.storageservice.controllers;

import com.peolly.storageservice.dto.ApiResponse;
import com.peolly.storageservice.dto.ItemDuplicateRequest;
import com.peolly.storageservice.exceptions.EmptyProductStorageAmountException;
import com.peolly.storageservice.external.ReserveItemsRequest;
import com.peolly.storageservice.models.Item;
import com.peolly.storageservice.services.ReservedItemService;
import com.peolly.storageservice.services.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;
    private final ReservedItemService reservedItemService;

    @PostMapping("/check-duplicate")
    public ResponseEntity<Boolean> isDuplicate(@RequestBody ItemDuplicateRequest request) {
        boolean isDuplicate = storageService.isDuplicate(request.name(), request.description());
        return ResponseEntity.ok(isDuplicate);
    }

    @PostMapping("/get-quantity")
    public ResponseEntity<?> getQuantity(@RequestBody Long itemId) {
        Item item = storageService.findItemById(itemId);
        if (item == null || item.getQuantity() == 0) {
            return new ResponseEntity<>(new ApiResponse(false, "Item not found or is out of the stock."), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(item.getQuantity());
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Boolean> isAvailable(@RequestBody(required = false) Map<Long, Integer> itemAvailabilityRequest) {
        if (itemAvailabilityRequest == null || itemAvailabilityRequest.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean isAvailable = storageService.isAvailableInStorage(itemAvailabilityRequest);
        return isAvailable ? ResponseEntity.ok(true) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }


    @PostMapping("/reserve-item")
    public ResponseEntity<?> reserveItem(@RequestBody ReserveItemsRequest reserveItemsRequest) {
        try {
            storageService.reserveItemsInStorage(reserveItemsRequest.itemIds(), reserveItemsRequest.userId());
        } catch (EmptyProductStorageAmountException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/reserve-item")
    public ResponseEntity<?> deleteReserveItem(@RequestBody ReserveItemsRequest reserveItemsRequest) {
        try {
            reservedItemService.deleteReservedItemsFromStorage(reserveItemsRequest.itemIds(), reserveItemsRequest.userId());
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}