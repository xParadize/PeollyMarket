package com.peolly.paymentmicroservice.services;

import com.peolly.configserver.ModelMapperConfig;
import com.peolly.paymentmicroservice.dto.CardDto;
import com.peolly.paymentmicroservice.models.UncheckedCard;
import com.peolly.paymentmicroservice.repositories.UncheckedCardRepository;
import com.peolly.utilservice.events.SavePaymentMethodEvent;
import com.peolly.utilservice.events.WasPaymentMethodAddedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Import(ModelMapperConfig.class)
public class UncheckedCardService {

    private final UncheckedCardRepository uncheckedCardRepository;
    private final CardService cardService;
    private final CardDataValidator dataValidator;
    private final ModelMapper modelMapper;


    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, WasPaymentMethodAddedEvent> sendWasPaymentMethodAddedEvent;

    @Transactional
    @KafkaListener(topics = "send-save-payment-method", groupId = "org-deli-queuing-payment")
    public void consumeSendSendSavePaymentMethodEvent(SavePaymentMethodEvent savePaymentMethodEvent) throws ExecutionException, InterruptedException {

        var cardDto = CardDto.builder()
                .cardNumber(savePaymentMethodEvent.getCardNumber())
                .monthExpiration(savePaymentMethodEvent.getMonthExpiration())
                .yearExpiration(savePaymentMethodEvent.getYearExpiration())
                .cvv(savePaymentMethodEvent.getCvv())
                .availableMoney(savePaymentMethodEvent.getAvailableMoney())
                .build();

        saveUncheckedCard(cardDto);

        boolean isCardCredentialsCorrect = dataValidator.isCardValid(cardDto);

        if (isCardCredentialsCorrect) {
            processSuccessfulSavingPaymentMethod(cardDto, savePaymentMethodEvent);
        }

        else {
            processUnsuccessfulSavingPaymentMethod(cardDto, savePaymentMethodEvent);
        }
    }

    @Transactional
    public void processSuccessfulSavingPaymentMethod(CardDto cardDto, SavePaymentMethodEvent event) throws ExecutionException, InterruptedException {
        cardService.savePaymentMethod(cardDto, event.getUserId());
        deleteUncheckedCardById(cardDto.getCardNumber());
        sendPaymentMethodAddedSuccessful(event.getUserId());
    }

    @Transactional
    public void processUnsuccessfulSavingPaymentMethod(CardDto cardDto, SavePaymentMethodEvent event) throws ExecutionException, InterruptedException {
        deleteUncheckedCardById(cardDto.getCardNumber());
        sendPaymentMethodWasNotAdded(event.getUserId());
    }

    private void sendPaymentMethodEvent(UUID userId, boolean isSuccess) throws ExecutionException, InterruptedException {
        var success = WasPaymentMethodAddedEvent.builder()
                .successful(isSuccess)
                .build();

        ProducerRecord<String, WasPaymentMethodAddedEvent> record = new ProducerRecord<>(
                "send-was-payment-method-added",
                userId.toString(),
                success
        );

        SendResult<String, WasPaymentMethodAddedEvent> result = sendWasPaymentMethodAddedEvent
                .send(record).get();

        LOGGER.info("Sent event: {}", result);
    }

    private void sendPaymentMethodAddedSuccessful(UUID userId) throws ExecutionException, InterruptedException {
        sendPaymentMethodEvent(userId, true);
    }

    private void sendPaymentMethodWasNotAdded(UUID userId) throws ExecutionException, InterruptedException {
        sendPaymentMethodEvent(userId, false);
    }

    @Transactional
    public void saveUncheckedCard(CardDto dtoToSave) {
        uncheckedCardRepository.save(dtoToUnchecked(dtoToSave));
    }

    @Transactional
    public void deleteUncheckedCardById(String id) {
        uncheckedCardRepository.deleteById(id);
    }

    private UncheckedCard dtoToUnchecked(CardDto dto) {
        return modelMapper.map(dto, UncheckedCard.class);
    }
}
