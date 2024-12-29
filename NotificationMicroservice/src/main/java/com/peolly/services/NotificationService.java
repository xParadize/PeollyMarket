package com.peolly.services;

import com.peolly.models.Notification;
import com.peolly.repositories.NotificationRepository;
import com.peolly.utilservice.events.PaymentMethodValidationResult;
import lombok.RequiredArgsConstructor;
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
    public void consumeSavePaymentMethodEvent(PaymentMethodValidationResult event) {
        if (event.valid()) {
            mailSenderService.sendCreditCartLinkedEmail(
                    event.email(),
                    event.cardNumber(),
                    true
            );

            telegramSenderService.sendCardLinkedMessage(event.cardNumber(), true);

            String cardLast4Nums = event.cardNumber().substring(event.cardNumber().length() - 4);
            saveNotification(
                    "Payment method added.",
                    String.format("Your card %s was added to payment methods.", cardLast4Nums),
                    event.userId()
            );
        } else {
            mailSenderService.sendCreditCartLinkedEmail(
                    event.email(),
                    event.cardNumber(),
                    false
            );

            telegramSenderService.sendCardLinkedMessage(event.cardNumber(), false);
            saveNotification(
                    "Payment method wasn't added.",
                    "Please, check card input data and send to validation again.",
                    event.userId()
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
}
