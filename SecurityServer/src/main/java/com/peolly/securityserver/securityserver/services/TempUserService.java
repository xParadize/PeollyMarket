package com.peolly.securityserver.securityserver.services;

import com.peolly.securityserver.kafka.SecurityKafkaProducer;
import com.peolly.securityserver.securityserver.models.TemporaryUser;
import com.peolly.securityserver.securityserver.repositories.TempUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TempUserService {
    private final TempUserRepository tempUserRepository;
    private final SecurityKafkaProducer securityKafkaProducer;

    @Transactional
    public void createTempUser(TemporaryUser tempUser) {
        tempUserRepository.save(tempUser);
        // securityKafkaProducer.sendCreateTemporaryUser(tempUser);
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
