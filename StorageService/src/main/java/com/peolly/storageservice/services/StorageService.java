package com.peolly.storageservice.services;

import com.peolly.schemaregistry.CreateItemEvent;
import com.peolly.storageservice.mappers.ItemMapper;
import com.peolly.storageservice.models.Item;
import com.peolly.storageservice.repositories.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemMapper itemMapper;

    @Transactional(readOnly = true)
    public boolean isDuplicate(String name, String description) {
        return storageRepository.existsByNameAndDescription(name, description);
    }

    @Transactional
    @KafkaListener(topics = "item-created", groupId = "org-deli-queuing-storage")
    public void consumeCreateItemEvent(CreateItemEvent event) {
         Item item = itemMapper.toEntity(event);
         saveItem(item);
    }

    @Transactional
    public void saveItem(Item item) {
        storageRepository.save(item);
    }
}
