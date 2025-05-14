package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.models.Card;
import com.peolly.paymentmicroservice.repositories.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardDataValidator {
    private final CardRepository cardRepository;

    public boolean isCardValid(CardDto dto, UUID userId) {
        return isCardExpirationDataValid(dto) && isCardBalanceSuitable(dto) && isCardNotInUse(dto, userId);
    }

    public boolean isCardValid(Card card, UUID userId, double totalCost) {
        return isCardExpirationDataValid(card) && isCardBalanceSuitable(card, totalCost) && isCardNotInUse(card, userId);
    }

    private boolean isCardExpirationDataValid(CardDto card) {
        int expireMonth = Integer.parseInt(card.getMonthExpiration());
        int expireYear = Integer.parseInt("20" + card.getYearExpiration());

        LocalDate expirationDate = LocalDate.of(expireYear, expireMonth, 1);
        return !expirationDate.isBefore(LocalDate.now().withDayOfMonth(1));
    }

    private boolean isCardExpirationDataValid(Card card) {
        int expireMonth = Integer.parseInt(card.getMonthExpiration());
        int expireYear = Integer.parseInt("20" + card.getYearExpiration());

        LocalDate expirationDate = LocalDate.of(expireYear, expireMonth, 1);

        boolean b = !expirationDate.isBefore(LocalDate.now().withDayOfMonth(1));
        return b;
    }

    private boolean isCardBalanceSuitable(CardDto dto) {
        if (dto.getAvailableMoney() < 11) {
            return false;
        }
        cardRepository.takeMoneyFromCard(BigDecimal.valueOf(11), dto.getCardNumber());
        cardRepository.addMoneyToCard(BigDecimal.valueOf(11), dto.getCardNumber());
        return true;
    }

    private boolean isCardBalanceSuitable(Card card, double totalCost) {
        if (card.getAvailableMoney() < totalCost) {
            return false;
        }
        cardRepository.takeMoneyFromCard(BigDecimal.valueOf(11), card.getCardNumber());
        return true;
    }

    private boolean isCardNotInUse(CardDto dto, UUID userId) {
        return cardRepository.isCardBelongToUser(dto.getCardNumber(), userId);
    }

    private boolean isCardNotInUse(Card card, UUID userId) {
        boolean b = cardRepository.isCardBelongToUser(card.getCardNumber(), userId);
        return b;
    }
}


