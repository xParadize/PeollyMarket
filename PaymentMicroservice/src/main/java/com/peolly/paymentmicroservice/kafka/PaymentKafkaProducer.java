package com.peolly.paymentmicroservice.kafka;

import com.peolly.utilservice.events.GetAllPaymentMethodsEvent;
import com.peolly.utilservice.events.PaymentMethodValidationResult;
import com.peolly.utilservice.events.PaymentMethodWasNotAddedEvent;
import com.peolly.utilservice.events.WasPaymentMethodDeletedEvent;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class PaymentKafkaProducer {
    private final KafkaTemplate<String, GetAllPaymentMethodsEvent> sendGetAllPaymentMethods;
    private final KafkaTemplate<String, WasPaymentMethodDeletedEvent> wasPaymentMethodDeletedEvent;
    private final KafkaTemplate<String, PaymentMethodValidationResult> sendCardValidationResult;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendGetAllCards(List<String> methodsToReturn) {
        GetAllPaymentMethodsEvent event = new GetAllPaymentMethodsEvent(methodsToReturn);
        ProducerRecord<String, GetAllPaymentMethodsEvent> record = new ProducerRecord<>(
                "send-get-all-payment-methods",
                "userId",
                event
        );
        sendGetAllPaymentMethods.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }

    public void sendWasPaymentMethodRemoved(UUID userId, boolean isSuccess) {
        WasPaymentMethodDeletedEvent event = new WasPaymentMethodDeletedEvent(isSuccess);
        ProducerRecord<String, WasPaymentMethodDeletedEvent> record = new ProducerRecord<>(
                "send-was-payment-method-deleted",
                userId.toString(),
                event
        );
        wasPaymentMethodDeletedEvent.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }

    public void sendPaymentMethodValidationResult(boolean isValid, UUID userId, String email, String cardNumber) {
        PaymentMethodValidationResult event = new PaymentMethodValidationResult(
                isValid,
                userId,
                email,
                cardNumber,
                LocalDateTime.now()
        );

        ProducerRecord<String, PaymentMethodValidationResult> record = new ProducerRecord<>(
                "send-payment-method-validation-result",
                "Payment Microservice",
                event
        );
        sendCardValidationResult.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
}
