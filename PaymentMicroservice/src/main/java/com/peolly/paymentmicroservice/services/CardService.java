package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.dto.CardMapper;
import com.peolly.paymentmicroservice.enums.CardType;
import com.peolly.paymentmicroservice.kafka.PaymentKafkaProducer;
import com.peolly.paymentmicroservice.models.Card;
import com.peolly.paymentmicroservice.repositories.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final PaymentKafkaProducer paymentKafkaProducer;
    private final CardMapper cardMapper;

//    @Transactional(readOnly = true)
//    public Card getCardNumberByUserId(UUID userId) {
//        Optional<Card> card = cardRepository.findByUserId(userId);
//        return card.orElse(null);
//    }

    @Transactional
    public void savePaymentMethod(CardDto dto, UUID userId) {
        Card cardToSave = convertDtoToCard(dto);
        cardToSave.setUserId(userId);
        cardToSave.setCardType(getCardType(dto.getCardNumber()).toString());
        cardRepository.save(cardToSave);
    }

    @Transactional
    public void takeMoneyFromCard(String cardNumber) {
        cardRepository.takeMoneyFromCard(BigDecimal.valueOf(11), cardNumber);
    }

    @Transactional
    public void addMoneyToCard(String cardNumber) {
        cardRepository.addMoneyToCard(BigDecimal.valueOf(11), cardNumber);
    }

    private CardType getCardType(String number) {
        for (CardType cardType : CardType.values()) {
            if (number.matches(cardType.getCardRegex())) {
                return cardType;
            }
        }
        return CardType.UNDEFINED_CARD;
    }

//    @Transactional(readOnly = true)
//    @KafkaListener(topics = "send-user-id-event", groupId = "org-deli-queuing-security")
//    public void getAllPaymentMethods(UserIdEvent userIdEvent) {
//        List<Card> userCards = cardRepository.findAllByUserId(userIdEvent.userId());
//        List<String> methodsToReturn = new ArrayList<>();
//
//        userCards.parallelStream()
//                .forEach(card -> methodsToReturn.add(card.getCardNumber()));
//        paymentKafkaProducer.sendGetAllCards(methodsToReturn);
//    }
//
//    @Transactional
//    @KafkaListener(topics = "send-delete-payment-method", groupId = "org-deli-queuing-payment")
//    public void consumeSendDeletePaymentMethod(DeletePaymentMethodEvent deletePaymentMethodEvent) {
//        String cardNumber = deletePaymentMethodEvent.cardNumber();
//        UUID userId = deletePaymentMethodEvent.userId();
//        Card userCard = getCardByCardNumber(cardNumber);
//
//        if (userCard != null && userCard.getUserId().equals(deletePaymentMethodEvent.userId())) {
//            processSuccessfulDeletingPaymentMethod(cardNumber, userId);
//        } else {
//            processUnsuccessfulSavingPaymentMethod(userId);
//        }
//    }

//    private void sendWasPaymentMethodDeleted(UUID userId, boolean isSuccess) {
//        paymentKafkaProducer.sendWasPaymentMethodRemoved(userId, isSuccess);
//    }

//    @Transactional
//    public void processSuccessfulDeletingPaymentMethod(String cardNumber, UUID userId) {
//        deletePaymentMethod(cardNumber);
//        sendPaymentMethodDeletedSuccessful(userId);
//    }
//
//    @Transactional
//    public void processUnsuccessfulSavingPaymentMethod(UUID userId) {
//        sendPaymentMethodDeletedUnsuccessful(userId);
//    }
//
////    private void sendPaymentMethodDeletedSuccessful(UUID userId) {
////        sendWasPaymentMethodDeleted(userId, true);
////    }
////
////    private void sendPaymentMethodDeletedUnsuccessful(UUID userId) {
////        sendWasPaymentMethodDeleted(userId, false);
////    }

    @Transactional(readOnly = true)
    public Card getCardByCardNumber(String cardNumber) {
        Optional<Card> card = cardRepository.findByCardNumber(cardNumber);
        return card.orElse(null);
    }

    @Transactional
    public void deletePaymentMethod(String cardNumber) {
        cardRepository.deleteCardByCardNumber(cardNumber);
    }


    @Transactional(readOnly = true)
    public boolean isCardBelongToUser(String cardNumber, UUID userId) {
        return cardRepository.isCardBelongToUser(cardNumber, userId);
    }

    private Card convertDtoToCard(CardDto dto) {
        return cardMapper.toEntity(dto);
    }
}
