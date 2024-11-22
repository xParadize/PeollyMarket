package com.peolly.paymentmicroservice.repositories;

import com.peolly.paymentmicroservice.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
    Optional<Card> findByUserId(UUID userId);
    void deleteCardByCardNumber(String cardNumber);
    List<Card> findAllByUserId(UUID userId);

    @Modifying
    @Query(value = "UPDATE card SET available_money = card.available_money - ?2 WHERE card_number = ?1", nativeQuery = true)
    void subtractMoneyForPurchase(String cardNumber, Double totalCost);

    @Modifying
    @Query(value = "UPDATE card SET available_money = card.available_money - 1 WHERE card_number = ?1", nativeQuery = true)
    void take1rub(String cardNumber);

    @Modifying
    @Query(value = "UPDATE card SET available_money = card.available_money + 1 WHERE card_number = ?1", nativeQuery = true)
    void give1rub(String cardNumber);

    @Query(value = "SELECT COUNT(*) > 0 FROM card WHERE card_number = ?1 AND user_id = ?2", nativeQuery = true)
    boolean isCardBelongToUser(String cardNumber, UUID userId);
}
