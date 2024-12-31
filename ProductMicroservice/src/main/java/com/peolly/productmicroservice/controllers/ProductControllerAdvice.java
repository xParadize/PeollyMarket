package com.peolly.productmicroservice.controllers;

import com.peolly.productmicroservice.dto.ApiResponse;
import com.peolly.productmicroservice.exceptions.CompanyHasNoProductsException;
import com.peolly.productmicroservice.exceptions.CompanyNotFoundException;
import com.peolly.productmicroservice.exceptions.IncorrectSearchPath;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class ProductControllerAdvice {
    @ExceptionHandler(IncorrectSearchPath.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleIncorrectSearchPath() {
        ApiResponse response = new ApiResponse(false, "There's noting here." +
                "Try going back or looking for something else.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleCompanyNotFoundException() {
        ApiResponse response = new ApiResponse(false, "This company doesn't exist.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CompanyHasNoProductsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse> handleCompanyHasNoProductsException() {
        ApiResponse response = new ApiResponse(false, "This company doesn't have any products.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
