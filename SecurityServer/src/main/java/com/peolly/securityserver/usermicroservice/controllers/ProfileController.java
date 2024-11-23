package com.peolly.securityserver.usermicroservice.controllers;

import com.peolly.securityserver.kafka.KafkaListenerFutureWaiter;
import com.peolly.securityserver.usermicroservice.exceptions.NoCreditCardLinkedException;
import com.peolly.securityserver.dto.CardData;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.services.UserService;
import com.peolly.utilservice.ApiResponse;
import com.peolly.utilservice.events.SavePaymentMethodEvent;
import com.peolly.utilservice.events.SendGetAllPaymentMethods;
import com.peolly.utilservice.events.SendUserIdEvent;
import com.peolly.utilservice.events.WasPaymentMethodAddedEvent;
import com.peolly.utilservice.exceptions.IncorrectSearchPath;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile")
public class ProfileController {

    private final UserService usersService;
    private final KafkaListenerFutureWaiter kafkaListenerFutureWaiter;
//    private final EcheckService echeckService;
//    private final UncheckedCardService uncheckedCardService;
//    private final NotificationService notificationService;
//    private final CollectUserInfoService collectUserInfoService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, SavePaymentMethodEvent> sendSavePaymentMethodEvent;
    private final KafkaTemplate<String, SendUserIdEvent> sendUserIdEvent;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

//    @Operation(summary = "Get all profile info")
//    @GetMapping()
//    public UserProfileDtoUser getUserInfo() {
//        User requestedUser = usersService.getCurrentUser();
//        return collectUserInfoService.getAllUserInfo(requestedUser);
//    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<String>> getAllPaymentMethods(Principal actualUser) throws ExecutionException, InterruptedException {
        User requestUser = usersService.findByUsername(actualUser.getName());
        SendUserIdEvent userIdEvent = new SendUserIdEvent(requestUser.getId());

        ProducerRecord<String, SendUserIdEvent> record = new ProducerRecord<>(
                "send-user-id-event",
                "userId",
                userIdEvent
        );

        SendResult<String, SendUserIdEvent> result = sendUserIdEvent
                .send(record).get();

        LOGGER.info("Sent event: {}", result);

        CompletableFuture<List<String>> future = new CompletableFuture<>();
        kafkaListenerFutureWaiter.setAllPaymentMethodsFuture(future);
        if (kafkaListenerFutureWaiter.getAllPaymentMethodsFuture().get().isEmpty()) {
            throw new NoCreditCardLinkedException();
        }

        return new ResponseEntity<>(future.get(), HttpStatus.OK);
    }

    @KafkaListener(topics = "send-get-all-payment-methods", groupId = "org-deli-queuing-security")
    public void consumeGetAllPaymentMethods(SendGetAllPaymentMethods paymentMethodsEvent) {
        CompletableFuture<List<String>> future = kafkaListenerFutureWaiter.getAllPaymentMethodsFuture();
        if (future != null) {
            future.complete(paymentMethodsEvent.paymentMethods());
        }
    }

    @Operation(summary = "There user should add their payment method (card data)")
    @PostMapping("/add-payment-method")
    public ResponseEntity<ApiResponse> addPaymentMethod(@RequestBody CardData cardData, Principal actualUser) throws ExecutionException, InterruptedException {

        User requestUser = usersService.findByUsername(actualUser.getName());
        UUID userId = requestUser.getId();

        var userCardData = SavePaymentMethodEvent.builder()
                .userId(userId)
                .cardNumber(cardData.getCardNumber())
                .monthExpiration(cardData.getMonthExpiration())
                .yearExpiration(cardData.getYearExpiration())
                .cvv(cardData.getCvv())
                .availableMoney(cardData.getAvailableMoney())
                .build();

        ProducerRecord<String, SavePaymentMethodEvent> record = new ProducerRecord<>(
                "send-save-payment-method",
                userId.toString(),
                userCardData
        );

        SendResult<String, SavePaymentMethodEvent> result = sendSavePaymentMethodEvent
                .send(record).get();

        LOGGER.info("Sent event: {}", result);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        kafkaListenerFutureWaiter.setPaymentMethodFuture(future);

        boolean isCardDataValid = future.get();
        if (isCardDataValid) {
            return new ResponseEntity<>(new ApiResponse(true, "Payment method added"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Incorrect card data or influenced balance"), HttpStatus.BAD_REQUEST);
        }
    }

    @KafkaListener(topics = "send-was-payment-method-added", groupId = "org-deli-queuing-security")
    public void consumeSendWasPaymentMethodAdded(WasPaymentMethodAddedEvent wasPaymentMethodAddedEvent) {
        CompletableFuture<Boolean> future = kafkaListenerFutureWaiter.getPaymentMethodFuture();
        if (future != null) {
            future.complete(wasPaymentMethodAddedEvent.isSuccessful());
        }
    }

//    @Operation(summary = "There user should delete their payment method (card data)")
//    @PostMapping("/delete-payment-method")
//    public ResponseEntity<ApiResponse> deletePaymentMethod(@RequestBody PaymentMethodDto dto, Principal actualUser) {
//
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        UUID userId = requestUser.getId();
//
//        Card userCard = cardService.getUserCardByCardNumber(dto.getCardNumber());
//
//        if (userCard != null && userCard.getUserId().equals(userId)) {
//            cardService.deletePaymentMethod(dto.getCardNumber());
//            return new ResponseEntity<>(new ApiResponse(true, "Payment method deleted"), HttpStatus.OK);
//        }
//
//        return new ResponseEntity<>(new ApiResponse(false, "Incorrect card data or this card doesn't belong to user"), HttpStatus.BAD_REQUEST);
//    }
//
//    @GetMapping("/check/{check_id}")
//    public ResponseEntity<Resource> downloadCheck(@PathVariable("check_id") String checkId) {
//        Echeck echeck = echeckService.getOneCheck(checkId);
//        String checkPath = echeck.getDownloadPath();
//        try {
//            File file = new File(checkPath);
//            if (!file.exists()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
//
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + echeck.getCheckNumber() + ".pdf")
//                    .contentType(MediaType.APPLICATION_PDF)
//                    .contentLength(file.length())
//                    .body(resource);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    @GetMapping("/e-checks")
//    public ResponseEntity<List<CheckDto>> getAllChecks(Principal actualUser) {
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        UUID userId = requestUser.getId();
//        List<CheckDto> checkDtoList = echeckService.getAllUserChecks(userId);
//        return new ResponseEntity<>(checkDtoList, HttpStatus.OK);
//    }
//
//    @GetMapping("/notifications")
//    public ResponseEntity<List<NotificationDto>> getAllNotifications(Principal actualUser) {
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        List<NotificationDto> notifications = notificationService.showAllNotifications(requestUser);
//        return new ResponseEntity<>(notifications, HttpStatus.OK);
//    }
}
