package com.peolly.storageservice.repositories;

import com.peolly.storageservice.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageRepository extends JpaRepository<Item, Long> {
    boolean existsByNameAndDescription(String name, String description);
    Optional<Item> findItemById(Long id);

    @Modifying
    @Query(value = "UPDATE items SET quantity = quantity - :amount WHERE id = :item_id", nativeQuery = true)
    void decreaseItemStorageAmount(@Param("item_id") Long itemId, @Param("amount") int amount);

    @Query(value = "SELECT CASE WHEN SUM(quantity) >= :amount THEN TRUE ELSE FALSE END FROM items WHERE id = :itemId", nativeQuery = true)
    boolean checkItemAvailability(@Param("itemId") Long itemId, @Param("amount") int amount);
}
