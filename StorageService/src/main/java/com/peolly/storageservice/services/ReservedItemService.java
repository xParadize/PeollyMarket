package com.peolly.storageservice.services;

import com.peolly.storageservice.models.ReservedItem;
import com.peolly.storageservice.repositories.ReservedItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservedItemService {
    private final ReservedItemRepository reservedItemRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Transactional
    // TODO: после резервации надо бы из кэша удалить товар
    public void reserveItem(Long itemId, UUID userId) {
        ReservedItem reservedItem = ReservedItem.builder()
                .itemId(itemId)
                .amount(1)
                .reservedBy(userId)
                .reservedAt(LocalDateTime.now())
                .build();
        reservedItemRepository.save(reservedItem);
    }

    @Transactional
    public void deleteReservedItemsFromStorage(List<Long> itemIds, UUID userId) {
        for (Long reservedItemId : itemIds) {
            reservedItemRepository.deleteReservedItemByReservedByAndItemId(userId, reservedItemId);
        }
    }
}
