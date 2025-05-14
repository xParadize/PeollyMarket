package com.peolly.ordermicroservice.repositories;

import com.peolly.ordermicroservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findOrderByUserIdAndId(UUID userId, Long id);
}
