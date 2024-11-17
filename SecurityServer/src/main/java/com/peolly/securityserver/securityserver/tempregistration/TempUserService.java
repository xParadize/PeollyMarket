package com.peolly.securityserver.securityserver.tempregistration;

import com.peolly.utilservice.events.SendEmailConfirmationTokenEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class TempUserService {

    private final TempUserRepository tempUserRepository;
    private final KafkaTemplate<String, SendEmailConfirmationTokenEvent> sendEmailConfirmationTokenEvent;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


    @Transactional
    public void create(TemporaryUser tempUser) throws ExecutionException, InterruptedException {
        tempUserRepository.save(tempUser);

        var tempUserData = SendEmailConfirmationTokenEvent.builder()
                .tempUserTokenId(tempUser.getId())
                .email(tempUser.getEmail())
                .build();

        ProducerRecord<String, SendEmailConfirmationTokenEvent> record = new ProducerRecord<>(
                "send-email-confirmation-token",
                tempUser.getId().toString(),
                tempUserData
        );

        SendResult<String, SendEmailConfirmationTokenEvent> result = sendEmailConfirmationTokenEvent
                .send(record).get();

        LOGGER.info("Sent event to topic 'send-email-confirmation-token': {}", result);
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
