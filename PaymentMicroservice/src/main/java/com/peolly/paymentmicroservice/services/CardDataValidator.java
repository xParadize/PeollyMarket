package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
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

    private boolean isCardExpirationDataValid(CardDto card) {
        int expireMonth = Integer.parseInt(card.getMonthExpiration());
        int expireYear = Integer.parseInt("20" + card.getYearExpiration());

        LocalDate expirationDate = LocalDate.of(expireYear, expireMonth, 1);
        return !expirationDate.isBefore(LocalDate.now().withDayOfMonth(1));
    }

    private boolean isCardBalanceSuitable(CardDto dto) {
        if (dto.getAvailableMoney() < 11) {
            return false;
        }
        cardService.takeMoneyFromCard(dto.getCardNumber());
        cardService.addMoneyToCard(dto.getCardNumber());
        return true;
    }

    private boolean isCardNotInUse(CardDto dto, UUID userId) {
        return !cardService.isCardBelongToUser(dto.getCardNumber(), userId);
    }
}


