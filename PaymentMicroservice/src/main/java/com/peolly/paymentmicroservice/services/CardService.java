package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.enums.CardType;
import com.peolly.paymentmicroservice.models.Card;
import com.peolly.paymentmicroservice.repositories.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
//    private final MailService mailService;
//    private final UserService usersService;
//
//    @Transactional(readOnly = true)
//    public Card getUserCardByCardNumber(String cardNumber) {
//        Optional<Card> card = cardRepository.findByCardNumber(cardNumber);
//        return card.orElse(null);
//    }
//
//    @Transactional(readOnly = true)
//    public Card getCardNumberByUserId(UUID userId) {
//        Optional<Card> card = cardRepository.findByUserId(userId);
//        return card.orElse(null);
//    }

    @Transactional
    public void savePaymentMethod(CardDto dto, UUID userId) {

        Card cardToSave = Card.builder()
                .userId(userId)
                .cardNumber(dto.getCardNumber())
                .monthExpiration(dto.getMonthExpiration())
                .yearExpiration(dto.getYearExpiration())
                .cvv(dto.getCvv())
                .availableMoney(dto.getAvailableMoney())
                .cardType(getCardType(dto.getCardNumber()).toString())
                .build();
        cardRepository.save(cardToSave);


        // mailService.sendCreditCartLinkedEmail(user, dto.getCardNumber());
    }

    @Transactional
    public void take1rub(String cardNumber) {
        cardRepository.take1rub(cardNumber);
    }

    @Transactional
    public void give1rub(String cardNumber) {
        cardRepository.give1rub(cardNumber);
    }

    private CardType getCardType(String number) {
        for (CardType cardType : CardType.values()) {
            if (number.matches(cardType.getCardRegex())) {
                return cardType;
            }
        }
        return CardType.UNDEFINED_CARD;
    }

//    @Transactional
//    public void deletePaymentMethod(String cardNumber) {
//        cardRepository.deleteCardByCardNumber(cardNumber);
//    }
//
//    @Transactional(readOnly = true)
//    public List<PaymentMethodDto> getAllPaymentMethods(UUID userId) {
//        List<Card> userCards = cardRepository.findAllByUserId(userId);
////        if (userCards.isEmpty()) throw new NoCreditCardLinkedException();
//        List<PaymentMethodDto> methodsToReturn = new ArrayList<>();
//
//        for (Card c : userCards) {
//            PaymentMethodDto dtoToAdd = PaymentMethodDto.builder()
//                    .cardNumber(c.getCardNumber())
//                    .build();
//            methodsToReturn.add(dtoToAdd);
//        }
//        return methodsToReturn;
//    }
//
//    @Transactional(readOnly = true)
//    public boolean isCardBelongToUser(String cardNumber, UUID userId) {
//        return cardRepository.isCardBelongToUser(cardNumber, userId);
//    }

//    private PaymentMethodDto convertCardToPaymentMethodDto(Card card) {
//        PaymentMethodDto dto = PaymentMethodDto.builder()
//                .cardNumber(card.getCardNumber())
//                .build();
//        return dto;
//    }
}
