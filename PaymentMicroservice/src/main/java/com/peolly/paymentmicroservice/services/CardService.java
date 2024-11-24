package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.enums.CardType;
import com.peolly.paymentmicroservice.models.Card;
import com.peolly.paymentmicroservice.repositories.CardRepository;
import com.peolly.utilservice.ApiResponse;
import com.peolly.utilservice.events.*;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, SendGetAllPaymentMethods> sendGetAllPaymentMethods;
    private final KafkaTemplate<String, WasPaymentMethodDeletedEvent> wasPaymentMethodDeletedEvent;
//    private final MailService mailService;
//    private final UserService usersService;
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

    @Transactional(readOnly = true)
    @KafkaListener(topics = "send-user-id-event", groupId = "org-deli-queuing-security")
    public void getAllPaymentMethods(SendUserIdEvent userIdEvent) throws ExecutionException, InterruptedException {
        List<Card> userCards = cardRepository.findAllByUserId(userIdEvent.userId());
        List<String> methodsToReturn = new ArrayList<>();

        userCards.parallelStream()
                .forEach(card -> methodsToReturn.add(card.getCardNumber()));

        SendGetAllPaymentMethods paymentMethods = new SendGetAllPaymentMethods(methodsToReturn);

        ProducerRecord<String, SendGetAllPaymentMethods> record = new ProducerRecord<>(
                "send-get-all-payment-methods",
                "userId",
                paymentMethods
        );

        SendResult<String, SendGetAllPaymentMethods> result = sendGetAllPaymentMethods
                .send(record).get();

        LOGGER.info("Sent event: {}", result);
    }

    @Transactional
    @KafkaListener(topics = "send-delete-payment-method", groupId = "org-deli-queuing-payment")
    public void consumeSendDeletePaymentMethod(SendDeletePaymentMethodEvent deletePaymentMethodEvent) throws ExecutionException, InterruptedException {

        String cardNumber = deletePaymentMethodEvent.cardNumber();
        UUID userId = deletePaymentMethodEvent.userId();
        Card userCard = getUserCardByCardNumber(cardNumber);

        if (userCard != null && userCard.getUserId().equals(deletePaymentMethodEvent.userId())) {
            processSuccessfulDeletingPaymentMethod(cardNumber, userId);
        } else {
            processUnsuccessfulSavingPaymentMethod(userId);
        }
    }

    private void sendWasPaymentMethodDeleted(UUID userId, boolean isSuccess) throws ExecutionException, InterruptedException {

        WasPaymentMethodDeletedEvent paymentMethodDeletedEvent = new WasPaymentMethodDeletedEvent(isSuccess);

        ProducerRecord<String, WasPaymentMethodDeletedEvent> record = new ProducerRecord<>(
                "send-was-payment-method-deleted",
                userId.toString(),
                paymentMethodDeletedEvent
        );

        SendResult<String, WasPaymentMethodDeletedEvent> result = wasPaymentMethodDeletedEvent
                .send(record).get();

        LOGGER.info("Sent event: {}", result);
    }

    @Transactional
    public void processSuccessfulDeletingPaymentMethod(String cardNumber, UUID userId) throws ExecutionException, InterruptedException {
        deletePaymentMethod(cardNumber);
        sendPaymentMethodDeletedSuccessful(userId);
    }

    @Transactional
    public void processUnsuccessfulSavingPaymentMethod(UUID userId) throws ExecutionException, InterruptedException {
        sendPaymentMethodDeletedUnsuccessful(userId);
    }

    private void sendPaymentMethodDeletedSuccessful(UUID userId) throws ExecutionException, InterruptedException {
        sendWasPaymentMethodDeleted(userId, true);
    }

    private void sendPaymentMethodDeletedUnsuccessful(UUID userId) throws ExecutionException, InterruptedException {
        sendWasPaymentMethodDeleted(userId, false);
    }

    @Transactional(readOnly = true)
    public Card getUserCardByCardNumber(String cardNumber) {
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

//    private PaymentMethodDto convertCardToPaymentMethodDto(Card card) {
//        PaymentMethodDto dto = PaymentMethodDto.builder()
//                .cardNumber(card.getCardNumber())
//                .build();
//        return dto;
//    }
}
