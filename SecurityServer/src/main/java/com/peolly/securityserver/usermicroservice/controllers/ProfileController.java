package com.peolly.securityserver.usermicroservice.controllers;

import com.peolly.securityserver.exceptions.IncorrectSearchPath;
import com.peolly.securityserver.exceptions.NoCreditCardLinkedException;
import com.peolly.securityserver.kafka.SecurityKafkaProducer;
import com.peolly.securityserver.usermicroservice.dto.ApiResponse;
import com.peolly.securityserver.usermicroservice.dto.CardData;
import com.peolly.securityserver.usermicroservice.dto.DeleteCardDto;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.services.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Tag(name = "Profile")
public class ProfileController {
    private final UserService usersService;
    private final SecurityKafkaProducer securityKafkaProducer;

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

//    @GetMapping("/payment-methods")
//    public ResponseEntity<List<String>> getAllPaymentMethods(Principal actualUser) throws ExecutionException, InterruptedException {
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        securityKafkaProducer.sendGetAllPaymentMethods(requestUser.getId());
//
//        CompletableFuture<List<String>> future = new CompletableFuture<>();
//        securityKafkaListenerFutureWaiter.setAllPaymentMethodsFuture(future);
//        if (securityKafkaListenerFutureWaiter.getAllPaymentMethodsFuture().get().isEmpty()) {
//            throw new NoCreditCardLinkedException();
//        }
//        return new ResponseEntity<>(future.get(), HttpStatus.OK);
//    }

//    @KafkaListener(topics = "send-get-all-payment-methods", groupId = "org-deli-queuing-security")
//    public void consumeGetAllPaymentMethods(GetAllPaymentMethodsEvent paymentMethodsEvent) {
//        CompletableFuture<List<String>> future = securityKafkaListenerFutureWaiter.getAllPaymentMethodsFuture();
//        if (future != null) {
//            future.complete(paymentMethodsEvent.paymentMethods());
//        }
//    }

    @Operation(summary = "There user should add their payment method (card data)")
    @PostMapping("/add-payment-method")
    public ResponseEntity<ApiResponse> addPaymentMethod(@RequestBody @Valid CardData cardData,
                                                        BindingResult bindingResult,
                                                        Principal actualUser) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        User requestUser = usersService.findByUsername(actualUser.getName());

        securityKafkaProducer.sendAddPaymentMethod(requestUser.getId(), requestUser.getEmail(), cardData);
        return new ResponseEntity<>(new ApiResponse(true, "Card sent to validation."), HttpStatus.OK);
    }

//    @Operation(summary = "There user should delete their payment method (card data)")
//    @DeleteMapping("/delete-payment-method")
//    public ResponseEntity<ApiResponse> deletePaymentMethod(@RequestBody DeleteCardDto deleteCardDto, Principal actualUser) throws ExecutionException, InterruptedException {
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        securityKafkaProducer.sendDeletePaymentMethod(requestUser, deleteCardDto);
//
//        CompletableFuture<Boolean> future = new CompletableFuture<>();
//        // securityKafkaListenerFutureWaiter.setWasPaymentMethodDeletedFuture(future);
//
//        boolean paymentMethodDeleted = future.get();
//        if (paymentMethodDeleted) {
//            return new ResponseEntity<>(new ApiResponse(true, "Payment method deleted"), HttpStatus.NO_CONTENT);
//        } else {
//            return new ResponseEntity<>(new ApiResponse(false, "Incorrect card data or this card doesn't belong to user"), HttpStatus.BAD_REQUEST);
//        }
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
    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return errorMessage;
    }
}

