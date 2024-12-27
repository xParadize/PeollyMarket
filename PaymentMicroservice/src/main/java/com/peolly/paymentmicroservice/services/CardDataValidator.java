package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.models.Card;
import com.peolly.paymentmicroservice.models.CardValidationErrorFields;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardDataValidator {
    private final CardService cardService;

    public CardValidationErrorFields isCardValid(CardDto dto, UUID userId) {
        boolean checkExpiration = isCardExpirationDataValid(dto);
        boolean checkBalance = isCardBalanceSuitable(dto);
        boolean checkUsage = isCardNotInUse(dto, userId);

        System.out.println("Exp: " + checkExpiration + " Bal: " + checkBalance + " Usa:" + checkUsage);

        return new CardValidationErrorFields(
                checkUsage && checkExpiration && checkBalance,
                checkExpiration,
                checkBalance,
                checkUsage
        );
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


