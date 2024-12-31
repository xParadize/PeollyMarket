package com.peolly.securityserver.kafka;

import com.peolly.schemaregistry.SavePaymentMethodEvent;
import com.peolly.securityserver.securityserver.models.TemporaryUser;
import com.peolly.securityserver.usermicroservice.dto.CardData;
import com.peolly.securityserver.usermicroservice.dto.DeleteCardDto;
import com.peolly.securityserver.usermicroservice.model.User;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@AllArgsConstructor
public class SecurityKafkaProducer {
//    private final KafkaTemplate<String, UserCreatedEvent> sendUserCreatedEmailEvent;
//    private final KafkaTemplate<String, UserIdEvent> sendUserIdEvent;
//    private final KafkaTemplate<String, SavePaymentMethodEvent> sendSavePaymentMethodEvent;
//    private final KafkaTemplate<String, DeletePaymentMethodEvent> sendDeletePaymentMethodEvent;
//    private final KafkaTemplate<String, EmailConfirmationTokenEvent> sendEmailConfirmationTokenEvent;
    private final KafkaTemplate<String, GenericRecord> sendSavePaymentMethodEvent;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

//    public void sendEmailConfirmed(String token, TemporaryUser temporaryUser) {
//        UserCreatedEvent event = new UserCreatedEvent(
//                token,
//                temporaryUser.getEmail(),
//                temporaryUser.getUsername()
//        );
//        ProducerRecord<String, UserCreatedEvent> record = new ProducerRecord<>(
//                "send-user-created-email",
//                token,
//                event
//        );
//        sendUserCreatedEmailEvent.send(record);
//        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
//    }
//
//    public void sendGetAllPaymentMethods(UUID userId) {
//        UserIdEvent userIdEvent = new UserIdEvent(userId);
//        ProducerRecord<String, UserIdEvent> record = new ProducerRecord<>(
//                "send-user-id-event",
//                userIdEvent
//        );
//        sendUserIdEvent.send(record);
//        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
//    }

    public void sendAddPaymentMethod(UUID userId, String email, CardData cardData) {
         SavePaymentMethodEvent event = SavePaymentMethodEvent.newBuilder()
                 .setUserId(userId)
                 .setEmail(email)
                 .setCardNumber(cardData.getCardNumber())
                 .setMonthExpiration(cardData.getMonthExpiration())
                 .setYearExpiration(cardData.getYearExpiration())
                 .setCvv(cardData.getCvv())
                 .setAvailableMoney(cardData.getAvailableMoney())
                 .build();
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(
                "send-save-payment-method",
                userId.toString(),
                event
        );
        sendSavePaymentMethodEvent.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }

//    public void sendDeletePaymentMethod(User requestUser, DeleteCardDto deleteCardDto) {
//        DeletePaymentMethodEvent deletePaymentMethodEvent = new DeletePaymentMethodEvent(
//                requestUser.getId(),
//                deleteCardDto.cardNumber()
//        );
//        ProducerRecord<String, DeletePaymentMethodEvent> record = new ProducerRecord<>(
//                "send-delete-payment-method",
//                "userId",
//                deletePaymentMethodEvent
//        );
//        sendDeletePaymentMethodEvent.send(record);
//        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
//    }
//
//    public void sendCreateTemporaryUser(TemporaryUser tempUser) {
//        EmailConfirmationTokenEvent event = new EmailConfirmationTokenEvent(
//                tempUser.getId(),
//                tempUser.getEmail()
//        );
//        ProducerRecord<String, EmailConfirmationTokenEvent> record = new ProducerRecord<>(
//                "send-email-confirmation-token",
//                tempUser.getId().toString(),
//                event
//        );
//        sendEmailConfirmationTokenEvent.send(record);
//        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
//    }
}
