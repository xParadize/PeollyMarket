package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.dto.CardMapper;
import com.peolly.paymentmicroservice.dto.UncheckedCardMapper;
import com.peolly.paymentmicroservice.kafka.PaymentKafkaProducer;
import com.peolly.paymentmicroservice.models.CardValidationErrorFields;
import com.peolly.paymentmicroservice.models.UncheckedCard;
import com.peolly.paymentmicroservice.repositories.UncheckedCardRepository;
import com.peolly.utilservice.events.SavePaymentMethodEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UncheckedCardService {
    private final UncheckedCardRepository uncheckedCardRepository;
    private final CardService cardService;
    private final CardDataValidator dataValidator;
    private final PaymentKafkaProducer paymentKafkaProducer;
    private final CardMapper cardMapper;
    private final UncheckedCardMapper uncheckedCardMapper;

    @Transactional
    @KafkaListener(topics = "send-save-payment-method", groupId = "org-deli-queuing-payment")
    public void consumeSavePaymentMethodEvent(SavePaymentMethodEvent event) {
        CardDto uncheckedCardData = convertEventToDto(event);
        saveUncheckedCard(uncheckedCardData);

        CardValidationErrorFields validationResult = dataValidator.isCardValid(uncheckedCardData, event.userId());
        if (validationResult.isCardDataValid()) {
            processSuccessfulSavingPaymentMethod(uncheckedCardData, event.userId(), validationResult);
        } else {
            processUnsuccessfulSavingPaymentMethod(uncheckedCardData.getCardNumber(), event.userId(), validationResult);
        }
    }

    @Transactional
    public void processSuccessfulSavingPaymentMethod(CardDto cardDto, UUID userId, CardValidationErrorFields errorFields) {
        cardService.savePaymentMethod(cardDto, userId);
        deleteUncheckedCardById(cardDto.getCardNumber());
        paymentKafkaProducer.sendWasPaymentMethodAdded(userId, errorFields);
    }

    @Transactional
    public void processUnsuccessfulSavingPaymentMethod(String cardNumber, UUID userId, CardValidationErrorFields errorFields) {
        deleteUncheckedCardById(cardNumber);
        paymentKafkaProducer.sendWasPaymentMethodAdded(userId, errorFields);
    }

    @Transactional
    public void saveUncheckedCard(CardDto dtoToSave) {
        uncheckedCardRepository.save(convertDtoToUncheckedCard(dtoToSave));
    }

    @Transactional
    public void deleteUncheckedCardById(String id) {
        uncheckedCardRepository.deleteById(id);
    }

    private UncheckedCard convertDtoToUncheckedCard(CardDto dto) {
        return uncheckedCardMapper.toEntity(dto);
    }

    private CardDto convertEventToDto(SavePaymentMethodEvent event) {
        return cardMapper.toDto(event);
    }
}
