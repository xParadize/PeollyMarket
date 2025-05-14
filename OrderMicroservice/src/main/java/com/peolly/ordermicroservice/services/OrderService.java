package com.peolly.ordermicroservice.services;

import com.peolly.ordermicroservice.exceptions.OrderNotFoundException;
import com.peolly.ordermicroservice.models.Order;
import com.peolly.ordermicroservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public Long createOrder(UUID userId, int totalItems, BigDecimal totalPrice, String cardNumber) {
        Order newOrder = Order.builder()
                .userId(userId)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .cardNumber(cardNumber)
                .status("pending")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        saveOrder(newOrder);
        return newOrder.getId();
    }

    @Transactional
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    @Transactional
    public void finishOrder(Long orderId, UUID userId) {
        Optional<Order> optOrder = orderRepository.findOrderByUserIdAndId(userId, orderId);
        Order order = optOrder.orElseThrow(() -> new OrderNotFoundException("Order not found."));

        order.setPaymentDate(LocalDateTime.now());
        order.setStatus("paid");
        order.setUpdatedAt(LocalDateTime.now());

        saveOrder(order);
    }
}
