//package com.peolly.paymentmicroservice.controllers;
//
//import com.peolly.paymentmicroservice.services.PaymentService;
//import com.peolly.utilservice.ApiResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.FileNotFoundException;
//import java.security.Principal;
//import java.util.UUID;
//
//@Tag(name = "Payment")
//@RestController
//@RequestMapping("/payment")
//@RequiredArgsConstructor
//public class PaymentController {
//
//    private final PaymentService paymentService;
//    private final CartService cartService;
//
//    @Operation(summary = "Perform payment")
//    @PostMapping()
//    public ResponseEntity<ApiResponse> pay(@RequestBody PaymentMethodDto paymentMethod, Principal actualUser) throws FileNotFoundException {
//
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        UUID userId = requestUser.getId();
//
//        if (!paymentService.isPaymentCredentialsOk(userId, paymentMethod.getCardNumber())) {
//            return new ResponseEntity<>(new ApiResponse(false, "Invalid card data. Please check"), HttpStatus.BAD_REQUEST);
//        }
//
//        if (!paymentService.isEnoughMoney(paymentMethod.getCardNumber() ,userId)) {
//            return new ResponseEntity<>(new ApiResponse(false, "Invalid card balance. Please top up"), HttpStatus.BAD_REQUEST);
//        }
//
//        if (cartService.isCartEmpty(userId)) {
//            return new ResponseEntity<>(new ApiResponse(false, "Cart is empty"), HttpStatus.BAD_REQUEST);
//        }
//
//        else {
//            paymentService.performPayment(userId, paymentMethod.getCardNumber());
//        }
//
//        return new ResponseEntity<>(new ApiResponse(true, "Your order is sent to delivery services."), HttpStatus.ACCEPTED);
//    }
//}
