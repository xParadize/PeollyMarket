package com.peolly.securityserver.usermicroservice.services;//package usermicroservice.services;
//
//
//import lombok.RequiredArgsConstructor;
//import org.deli.usermicroservice.dto.UserProfileDtoUser;
//import org.deli.usermicroservice.model.User;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class CollectUserInfoService {
//
//    @Value("${onlinestore.currency}")
//    private String currency;
//
//    private final CardService cardService;
//    private final AuthDeviceInfoService authDeviceInfoService;
//    private final PaymentService paymentService;
//
//    @Transactional(readOnly = true)
//    public UserProfileDtoUser getAllUserInfo(User user) {
//        UserProfileDtoUser info = UserProfileDtoUser.builder()
//                .username(user.getUsername())
//                .phoneNumber(user.getPhoneNumber())
//                .email(user.getEmail())
//                .birthDate(user.getBirthDate())
//                .sex(user.getSex())
//                .sessionsAndDevices(authDeviceInfoService.getAllSessions(user))
//                .paymentMethods(cardService.getAllPaymentMethods(user.getId()))
//                .currency(currency)
//                .goodsPurchased(paymentService.getUserGoodsPurchased(user))
//                .moneySpent(paymentService.getUserMoneySpent(user))
//                .feedbacks(null)
//                .build();
//        return info;
//    }
//}
