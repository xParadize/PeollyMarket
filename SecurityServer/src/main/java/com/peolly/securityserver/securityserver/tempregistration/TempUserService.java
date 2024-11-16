package com.peolly.securityserver.securityserver.tempregistration;

import com.peolly.utilservice.events.SendEmailConfirmationTokenEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TempUserService {

    private final TempUserRepository tempUserRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void create(TemporaryUser tempUser) {
        tempUserRepository.save(tempUser);

        var tempUserData = SendEmailConfirmationTokenEvent.builder()
                .tempUserTokenId(tempUser.getId())
                .email(tempUser.getEmail())
                .build();

        kafkaTemplate.send("send-email-confirmation-token", tempUserData);
    }

    @Transactional
    public Optional<TemporaryUser> findTempUserById(UUID id) {
        return tempUserRepository.findById(String.valueOf(id));
    }

    @Transactional
    public void deleteTempUserById(UUID id) {
        tempUserRepository.deleteById(String.valueOf(id));
    }
}
