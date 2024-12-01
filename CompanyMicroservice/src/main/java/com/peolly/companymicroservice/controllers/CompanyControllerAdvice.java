package com.peolly.companymicroservice.controllers;

import com.peolly.companymicroservice.exceptions.IncorrectSearchPath;
import com.peolly.companymicroservice.exceptions.CompanyNotFoundException;
import com.peolly.utilservice.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class CompanyControllerAdvice {

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
        ApiResponse response = new ApiResponse(false, "Company not found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
