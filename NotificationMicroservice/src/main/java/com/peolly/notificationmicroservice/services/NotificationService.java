package com.peolly.notificationmicroservice.services;

import com.peolly.notificationmicroservice.models.Notification;
import com.peolly.notificationmicroservice.repositories.NotificationRepository;
import com.peolly.schemaregistry.PaymentMethodValidationResult;
import lombok.RequiredArgsConstructor;
import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MailSenderService mailSenderService;
    private final TelegramSenderService telegramSenderService;
    private final NotificationRepository notificationRepository;

    @Transactional
    @KafkaListener(topics = "send-payment-method-validation-result", groupId = "org-deli-queuing-notification")
    public void consumeSavePaymentMethodEvent(ConsumerRecord<String, GenericRecord> message) {
        SpecificData specificData = configureSpecificData();

        PaymentMethodValidationResult event = (PaymentMethodValidationResult) specificData.deepCopy(
                PaymentMethodValidationResult.SCHEMA$, message.value());
        UUID userId = UUID.fromString(event.getUserId().toString());

        if (event.getValid()) {
            mailSenderService.sendCreditCartLinkedEmail(
                    event.getEmail().toString(),
                    event.getCardNumber().toString(),
                    true
            );

            telegramSenderService.sendCardLinkedMessage(event.getCardNumber().toString(), true);

            String cardLast4Nums = event
                    .getCardNumber().toString().substring(event.getCardNumber().length() - 4);
            saveNotification(
                    "Payment method added.",
                    String.format("Your card *%s was added to payment methods.", cardLast4Nums),
                    userId);
        } else {
            mailSenderService.sendCreditCartLinkedEmail(
                    event.getEmail().toString(),
                    event.getCardNumber().toString(),
                    false
            );

            telegramSenderService.sendCardLinkedMessage(event.getCardNumber().toString(), false);
            saveNotification(
                    "Payment method wasn't added.",
                    "Please, check card input data and send to validation again.",
                    userId
            );
        }
    }

    @Transactional
    public void saveNotification(String header, String message, UUID userId) {
        Notification notification = Notification.builder()
                .header(header)
                .message(message)
                .notifiedAt(LocalDateTime.now())
                .recipient(userId)
                .build();
        notificationRepository.save(notification);
    }

    private SpecificData configureSpecificData() {
        SpecificData specificData = new SpecificData();
        specificData.addLogicalTypeConversion(new Conversions.UUIDConversion());
        specificData.addLogicalTypeConversion(new TimeConversions.TimestampMicrosConversion());
        return specificData;
    }
}