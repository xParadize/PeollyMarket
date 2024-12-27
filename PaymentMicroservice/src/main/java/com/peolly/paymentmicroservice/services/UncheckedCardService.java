package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.dto.CardMapper;
import com.peolly.paymentmicroservice.dto.UncheckedCardMapper;
import com.peolly.paymentmicroservice.kafka.PaymentKafkaProducer;
import com.peolly.paymentmicroservice.models.UncheckedCard;
import com.peolly.paymentmicroservice.repositories.UncheckedCardRepository;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.deli.queuing.payment.SavePaymentMethodEvent;
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
    public void consumeSavePaymentMethodEvent(ConsumerRecord<String, GenericRecord> message) {
        SavePaymentMethodEvent event = (SavePaymentMethodEvent) SpecificData
                .get()
                .deepCopy(SavePaymentMethodEvent.SCHEMA$, message.value());

        CardDto uncheckedCardData = convertEventToDto(event);
        saveUncheckedCard(uncheckedCardData);

        UUID userId = UUID.fromString(event.getUserId().toString());


        boolean isCardValid = dataValidator.isCardValid(uncheckedCardData, userId);
        if (isCardValid) {
            processSuccessfulSavingPaymentMethod(uncheckedCardData, userId);
        } else {
            processUnsuccessfulSavingPaymentMethod(uncheckedCardData.getCardNumber(), userId);
        }
    }

    @Transactional
    public void processSuccessfulSavingPaymentMethod(CardDto cardDto, UUID userId) {
        cardService.savePaymentMethod(cardDto, userId);
        deleteUncheckedCardById(cardDto.getCardNumber());
        System.out.println("Payment method added");
        // paymentKafkaProducer.sendWasPaymentMethodAdded(userId, errorFields);
    }

    @Transactional
    public void processUnsuccessfulSavingPaymentMethod(String cardNumber, UUID userId) {
        deleteUncheckedCardById(cardNumber);
        System.out.println("Payment method NOT added");
        // paymentKafkaProducer.sendWasPaymentMethodAdded(userId, errorFields);
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
