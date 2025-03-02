package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.models.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardDataValidator {
    private final CardService cardService;

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
        return !expirationDate.isBefore(LocalDate.now().withDayOfMonth(1));
    }

    private boolean isCardBalanceSuitable(CardDto dto) {
        if (dto.getAvailableMoney() < 11) {
            return false;
        }
        cardService.takeMoneyFromCard(dto.getCardNumber(), 11);
        cardService.addMoneyToCard(dto.getCardNumber(), 11);
        return true;
    }

    private boolean isCardBalanceSuitable(Card card, double totalCost) {
        if (card.getAvailableMoney() < totalCost) {
            return false;
        }
        cardService.takeMoneyFromCard(card.getCardNumber(), totalCost);
        return true;
    }

    private boolean isCardNotInUse(CardDto dto, UUID userId) {
        return !cardService.isCardBelongToUser(dto.getCardNumber(), userId);
    }

    private boolean isCardNotInUse(Card card, UUID userId) {
        return !cardService.isCardBelongToUser(card.getCardNumber(), userId);
    }
}


