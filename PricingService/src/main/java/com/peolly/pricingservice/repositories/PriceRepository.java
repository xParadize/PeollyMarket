package com.peolly.pricingservice.repositories;

import com.peolly.pricingservice.models.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    @Query(value = """
        SELECT COALESCE(
            CASE
                WHEN d.discount_type = 'PERCENT' THEN p.price * (1 - d.discount_value / 100)
                WHEN d.discount_type = 'FIXED' THEN p.price - d.discount_value
                ELSE p.price
            END,
            p.price
        ) AS updatedPrice
        FROM prices p
        LEFT JOIN discounts d ON p.item_id = d.item_id
                              AND d.is_active = TRUE
                              AND (d.start_date IS NULL OR d.start_date <= NOW())
                              AND (d.end_date IS NULL OR d.end_date >= NOW())
        WHERE p.item_id = :itemId
        LIMIT 1
    """, nativeQuery = true)
    double getUpdatedPriceForItem(@Param("itemId") Long itemId);
}
