package com.peolly.securityserver.usermicroservice.controllers;

import com.peolly.securityserver.exceptions.*;
import com.peolly.utilservice.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class SecurityControllerAdvice {
    @ExceptionHandler(IncorrectSearchPath.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleIncorrectSearchPath() {
        ApiResponse response = new ApiResponse(false, "There's noting here." +
                "Try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoCreditCardLinkedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse> handleNoCreditCardLinkedException() {
        ApiResponse response = new ApiResponse(false, "Payment methods are missing");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(IncorrectRoleInput.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleIncorrectRoleInput() {
        ApiResponse response = new ApiResponse(false, "This role doesn't exist.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RepeatedRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleRepeatedRoleException() {
        ApiResponse response = new ApiResponse(false, "User already has this role.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleMissingRoleException() {
        ApiResponse response = new ApiResponse(false, "User doesn't have this role to remove.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailConfirmationTokenExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleEmailConfirmationTokenExpiredException() {
        ApiResponse response = new ApiResponse(false, "Confirmation token has expired.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
