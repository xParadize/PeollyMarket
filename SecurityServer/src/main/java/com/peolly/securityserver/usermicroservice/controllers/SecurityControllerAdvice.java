package com.peolly.securityserver.usermicroservice.controllers;

import com.peolly.securityserver.usermicroservice.exceptions.IncorrectSearchPath;
import com.peolly.securityserver.usermicroservice.exceptions.NoCreditCardLinkedException;
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

    @ExceptionHandler(NoCreditCardLinkedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse> handleNoCreditCardLinkedException() {
        ApiResponse response = new ApiResponse(false, "Payment methods are missing");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(IncorrectSearchPath.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleIncorrectSearchPath() {
        ApiResponse response = new ApiResponse(false, "There's noting here." +
                "Try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
