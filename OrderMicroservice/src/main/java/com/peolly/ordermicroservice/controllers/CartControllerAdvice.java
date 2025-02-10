package com.peolly.ordermicroservice.controllers;

import com.peolly.ordermicroservice.dto.ApiResponse;
import com.peolly.ordermicroservice.exceptions.ProductNotFoundException;
import com.peolly.ordermicroservice.exceptions.UserServiceUnavailableException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class CartControllerAdvice {
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleProductNotFoundException(ProductNotFoundException e) {
        return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ApiResponse> handleUserServiceUnavailable(UserServiceUnavailableException e) {
        return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }
}

