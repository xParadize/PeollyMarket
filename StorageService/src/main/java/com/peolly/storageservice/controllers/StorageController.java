package com.peolly.storageservice.controllers;

import com.peolly.storageservice.dto.ApiResponse;
import com.peolly.storageservice.dto.ItemDuplicateRequest;
import com.peolly.storageservice.exceptions.EmptyProductStorageAmountException;
import com.peolly.storageservice.exceptions.IncorrectSearchPath;
import com.peolly.storageservice.external.ReserveItemsRequest;
import com.peolly.storageservice.models.Item;
import com.peolly.storageservice.services.ReservedItemService;
import com.peolly.storageservice.services.StorageService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@Tag(name = "Storage controller")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;
    private final ReservedItemService reservedItemService;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Check if an item(s) already exist(s)")
    @PostMapping("/items/check-duplicate")
    public ResponseEntity<List<Boolean>> isDuplicate(@RequestBody List<ItemDuplicateRequest> requests) {
        List<Boolean> result = storageService.isDuplicate(requests);
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "Get the quantity of goods in stock")
    @GetMapping("/items/{id}/quantity")
    public ResponseEntity<?> getQuantity(@PathVariable("id") Long itemId) {
        Item item = storageService.findItemById(itemId);
        if (item == null || item.getQuantity() == 0) {
            return new ResponseEntity<>(new ApiResponse(false, "Item not found or is out of the stock."), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(item.getQuantity());
    }

    @Operation(summary = "Check if all the requested items are in stock")
    @PostMapping("/items/check-stock")
    public ResponseEntity<Boolean> isAvailable(@RequestBody(required = false) Map<Long, Integer> itemAvailabilityRequest) {
        if (itemAvailabilityRequest == null || itemAvailabilityRequest.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean isAvailable = storageService.isAvailableInStorage(itemAvailabilityRequest);
        return isAvailable ? ResponseEntity.ok(true) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @Operation(summary = "Reserve items in stock")
    @PostMapping("/reservations")
    public ResponseEntity<?> reserveItem(@RequestBody ReserveItemsRequest reserveItemsRequest) {
        try {
            storageService.reserveItemsInStorage(reserveItemsRequest.itemIds(), reserveItemsRequest.userId());
        } catch (EmptyProductStorageAmountException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Remove items from reservation")
    @DeleteMapping("/reservations")
    public ResponseEntity<?> deleteReserveItem(@RequestBody ReserveItemsRequest reserveItemsRequest) {
        try {
            reservedItemService.deleteReservedItemsFromStorage(reserveItemsRequest.itemIds(), reserveItemsRequest.userId());
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}