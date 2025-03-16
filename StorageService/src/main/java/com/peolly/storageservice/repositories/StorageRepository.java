package com.peolly.storageservice.repositories;

import com.peolly.storageservice.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends JpaRepository<Item, Long> {
    boolean existsByNameAndDescription(String name, String description);
}
