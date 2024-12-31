package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.dto.CardMapper;
import com.peolly.paymentmicroservice.dto.UncheckedCardMapper;
import com.peolly.paymentmicroservice.kafka.PaymentKafkaProducer;
import com.peolly.paymentmicroservice.models.UncheckedCard;
import com.peolly.paymentmicroservice.repositories.UncheckedCardRepository;
import com.peolly.schemaregistry.SavePaymentMethodEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.avro.Conversions;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
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
        SpecificData specificData = configureSpecificData();

        SavePaymentMethodEvent event = (SavePaymentMethodEvent) specificData.deepCopy(
                SavePaymentMethodEvent.SCHEMA$, message.value());
        UUID userId = UUID.fromString(event.getUserId().toString());

        CardDto uncheckedCardData = convertEventToDto(event);
        saveUncheckedCard(uncheckedCardData);

        boolean isCardValid = dataValidator.isCardValid(uncheckedCardData, userId);
        if (isCardValid) {
            processSuccessfulSavingPaymentMethod(uncheckedCardData, userId, event.getEmail().toString());
        } else {
            processUnsuccessfulSavingPaymentMethod(uncheckedCardData, userId, event.getEmail().toString());
        }
    }

    @Transactional
    public void processSuccessfulSavingPaymentMethod(CardDto cardDto, UUID userId, String email) {
        cardService.savePaymentMethod(cardDto, userId);
        deleteUncheckedCardById(cardDto.getCardNumber());
        paymentKafkaProducer.sendPaymentMethodValidationResult(true, userId, email, cardDto.getCardNumber());
    }

    @Transactional
    public void processUnsuccessfulSavingPaymentMethod(CardDto cardDto, UUID userId, String email) {
        deleteUncheckedCardById(cardDto.getCardNumber());
        paymentKafkaProducer.sendPaymentMethodValidationResult(false, userId, email, cardDto.getCardNumber());
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

    private SpecificData configureSpecificData() {
        SpecificData specificData = new SpecificData();
        specificData.addLogicalTypeConversion(new Conversions.UUIDConversion());
        return specificData;
    }
}
