package com.peolly.services;

import com.peolly.models.EmailConfirmation;
import com.peolly.repositories.EmailConfirmationRepository;
import com.peolly.utilservice.events.SendEmailConfirmationTokenEvent;
import com.peolly.utilservice.events.SendUserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {

    private final EmailConfirmationRepository emailConfirmationRepository;
    private final MailService mailService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Transactional
    @KafkaListener(topics = "send-email-confirmation-token", groupId = "org-deli-queuing-email")
    public void consumeJson(SendEmailConfirmationTokenEvent emailConfirmationTokenEvent) {

        LOGGER.info("Received email from topic 'send-email-confirmation-token': {}", emailConfirmationTokenEvent.getEmail());

        var confirmation = EmailConfirmation.builder()
                .token(String.valueOf(emailConfirmationTokenEvent.getTempUserTokenId()))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        saveConfirmationToken(confirmation);

        mailService.sendVerifyEmail(emailConfirmationTokenEvent.getEmail(), String.valueOf(confirmation.getToken()));
    }

    @Transactional
    @KafkaListener(topics = "send-user-created-email", groupId = "org-deli-queuing-email")
    public void consumeSendUserCreatedEvent(SendUserCreatedEvent userCreatedEvent) {

        setConfirmedAt(userCreatedEvent.getUserToken());

        mailService.sendRegistrationEmail(
                userCreatedEvent.getEmail(),
                userCreatedEvent.getUsername()
        );
    }

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
