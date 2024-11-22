package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.enums.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CardDataValidator {

    private final CardService cardService;

    public boolean isCardValid(CardDto dto) {
        return isCardDataValid(dto) && takeAndGive1rub(dto);
    }

    private boolean isCardDataValid(CardDto card) {

        boolean validDate;
        boolean validCardNumber = false;

        int expireMonth = Integer.parseInt(card.getMonthExpiration());
        int expireYear = Integer.parseInt("20" + card.getYearExpiration());

        if (expireYear > LocalDateTime.now().getYear()) {
            validDate = true;
        } else {
            if (expireYear != LocalDateTime.now().getYear()) {
                validDate = false;
            } else {
                validDate = expireMonth > LocalDateTime.now().getMonthValue();
            }
        }

        for (CardType cardType : CardType.values()) {
            if (card.getCardNumber().matches(cardType.getCardRegex())) {
                validCardNumber = true;
                break;
            }
        }

        return validDate && validCardNumber;
    }

    private boolean takeAndGive1rub(CardDto dto) {
        if (dto.getAvailableMoney() >= 1) {
            cardService.take1rub(dto.getCardNumber());
            cardService.give1rub(dto.getCardNumber());
            return true;
        }
        return false;
    }
}


