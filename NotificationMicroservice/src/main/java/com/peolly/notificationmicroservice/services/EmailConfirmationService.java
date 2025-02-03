package com.peolly.notificationmicroservice.services;

import com.peolly.notificationmicroservice.models.EmailConfirmation;
import com.peolly.notificationmicroservice.repositories.EmailConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final MailSenderService mailService;

//    @Transactional
//    @KafkaListener(topics = "send-email-confirmation-token", groupId = "org-deli-queuing-notification")
//    public void consumeEmailConfirmationTokenEvent(EmailConfirmationTokenEvent emailConfirmationTokenEvent) {
//        var confirmation = EmailConfirmation.builder()
//                .token(emailConfirmationTokenEvent.email())
//                .createdAt(LocalDateTime.now())
//                .expiresAt(LocalDateTime.now().plusMinutes(15))
//                .build();
//        saveConfirmationToken(confirmation);
//        mailService.sendVerifyEmail(emailConfirmationTokenEvent.email(), String.valueOf(confirmation.getToken()));
//    }
//
//    @Transactional
//    @KafkaListener(topics = "send-user-created-email", groupId = "org-deli-queuing-notification")
//    public void consumeUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
//        setConfirmedAt(userCreatedEvent.userToken());
//        mailService.sendRegistrationEmail(
//                userCreatedEvent.email(),
//                userCreatedEvent.username()
//        );
//    }

    @Transactional
    public void saveConfirmationToken(EmailConfirmation token) {
        emailConfirmationRepository.save(token);
    }

    @Transactional(readOnly = true)
    public Optional<EmailConfirmation> getToken(String token) {
        return emailConfirmationRepository.findByToken(token);
    }

    @Transactional
    public void setConfirmedAt(String token) {
        emailConfirmationRepository.updateConfirmedAt(token, LocalDateTime.now());
    }
}