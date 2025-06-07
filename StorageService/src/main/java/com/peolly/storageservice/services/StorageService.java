package com.peolly.storageservice.services;

import com.peolly.schemaregistry.CreateItemEvent;
import com.peolly.storageservice.dto.ItemDuplicateRequest;
import com.peolly.storageservice.exceptions.EmptyProductStorageAmountException;
import com.peolly.storageservice.mappers.ItemMapper;
import com.peolly.storageservice.models.Item;
import com.peolly.storageservice.repositories.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final ReservedItemService reservedItemService;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public Item findItemById(Long itemId) {
        return storageRepository.findItemById(itemId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Boolean> isDuplicate(List<ItemDuplicateRequest> requests) {
        List<Boolean> duplicateCheckResult = requests.stream()
                .map(i -> storageRepository.existsByNameAndDescription(i.name(), i.description()))
                .toList();
        return duplicateCheckResult;
    }

    @Transactional(readOnly = true)
    public boolean isAvailableInStorage(Map<Long, Integer> itemAvailabilityRequest) {
        return itemAvailabilityRequest.entrySet().stream()
                .allMatch(entry -> storageRepository.checkItemAvailability(entry.getKey(), entry.getValue()));
    }


    @Transactional
    public void reserveItemsInStorage(List<Long> itemIds, UUID userId) {
        // TODO: надо бы сделать мапу чтобы резервировать не по 1 товару, а сразу по N раз, чтобы не нагружать БД

        for (Long itemId : itemIds) {
            Item itemToReserve = storageRepository.findItemById(itemId).orElse(null);

            if (itemToReserve == null) {
                throw new EmptyProductStorageAmountException("Item not found. Please refresh cart info.");
            }

            if (itemToReserve.getQuantity() <= 0) {
                throw new EmptyProductStorageAmountException("Item is out of stock. Please refresh cart info.");
            }
            reservedItemService.reserveItem(itemId, userId);
            decreaseProductStorageAmount(itemId, 1);
        }
    }

    @Transactional
    public void decreaseProductStorageAmount(Long itemId, int amount) {
        storageRepository.decreaseItemStorageAmount(itemId, amount);
    }

    @Transactional
    @KafkaListener(topics = "item-created", groupId = "org-deli-queuing-storage")
    public void consumeCreateItemEvent(CreateItemEvent event) {
         Item item = itemMapper.toEntity(event);
         item.setCompanyId((long) 1);
         saveItem(item);
    }

    @Transactional
    public void saveItem(Item item) {
        storageRepository.save(item);
    }
}
