package com.peolly.securityserver.kafka;

import com.peolly.schemaregistry.ConfirmUserEmailEvent;
import com.peolly.schemaregistry.CreateUserAccountEvent;
import com.peolly.schemaregistry.SavePaymentMethodEvent;
import com.peolly.securityserver.securityserver.models.TemporaryUser;
import com.peolly.securityserver.usermicroservice.dto.CardData;
import lombok.AllArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class SecurityKafkaProducer {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void sendCreateTemporaryUser(TemporaryUser tempUser) {
        CreateUserAccountEvent event = CreateUserAccountEvent.newBuilder()
                .setUserId(tempUser.getId())
                .setEmail(tempUser.getEmail())
                .build();
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(
                "email-confirmation-token",
                "Security Microservice",
                event
        );
        kafkaTemplate.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }

    public void sendEmailConfirmed(String token, TemporaryUser temporaryUser) {
        ConfirmUserEmailEvent event = new ConfirmUserEmailEvent(
                UUID.fromString(token),
                temporaryUser.getEmail(),
                temporaryUser.getUsername()
        );
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(
                "user-email-confirmation",
                "Security Server",
                event
        );
        kafkaTemplate.send(record);
        LOGGER.info("message written at topic '{}': {} = {}", record.topic(), record.key(), record.value());
    }
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
                "Security Microservice",
                event
        );
        kafkaTemplate.send(record);
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

}
