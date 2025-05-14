package com.peolly.storageservice.repositories;

import com.peolly.storageservice.models.ReservedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReservedItemRepository extends JpaRepository<ReservedItem, Long> {
    void deleteReservedItemByReservedByAndItemId(UUID reservedBy, Long itemId);
}
