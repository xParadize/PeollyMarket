//package com.peolly.paymentmicroservice.services;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class EcheckService {
//
//    private final EcheckRepository echeckRepository;
//
//    @Transactional
//    public void saveCheck(String number, UUID userId, String path) {
//        Echeck echeckToSave = Echeck.builder()
//                .checkNumber(number)
//                .userId(userId)
//                .downloadPath(path)
//                .receivedAt(LocalDateTime.now())
//                .build();
//        echeckRepository.save(echeckToSave);
//    }
//
//    @Transactional(readOnly = true)
//    public Echeck getOneCheck(String checkNumber) {
//        Optional<Echeck> echeck = echeckRepository.findByCheckNumber(checkNumber);
//        return echeck.get();
//    }
//
//    @Transactional(readOnly = true)
//    public List<CheckDto> getAllUserChecks(UUID userId) {
//
//        List<Echeck> echecks = echeckRepository.findAllByUserId(userId);
//        List<CheckDto> checksToReturn = new ArrayList<>();
//
//        for (Echeck e : echecks) {
//            CheckDto dto = CheckDto.builder()
//                    .downloadPath(String.format("http://localhost:8080/profile/check/%s", e.getCheckNumber()))
//                    .receivedAt(e.getReceivedAt())
//                    .build();
//            checksToReturn.add(dto);
//        }
//
//        return checksToReturn;
//    }
//}
