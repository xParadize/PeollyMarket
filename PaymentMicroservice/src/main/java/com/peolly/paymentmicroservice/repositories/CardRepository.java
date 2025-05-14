package com.peolly.paymentmicroservice.repositories;

import com.peolly.paymentmicroservice.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
    void deleteCardByCardNumber(String cardNumber);

    @Modifying
    @Query(value = "UPDATE card SET available_money = card.available_money - ?1 WHERE card_number = ?2", nativeQuery = true)
    void takeMoneyFromCard(BigDecimal amount, String cardNumber);

    @Modifying
    @Query(value = "UPDATE card SET available_money = card.available_money + ?1 WHERE card_number = ?2", nativeQuery = true)
    void addMoneyToCard(BigDecimal amount, String cardNumber);

    @Query(value = "SELECT COUNT(*) > 0 FROM card WHERE card_number = ?1 AND user_id = ?2", nativeQuery = true)
    boolean isCardBelongToUser(String cardNumber, UUID userId);
}
