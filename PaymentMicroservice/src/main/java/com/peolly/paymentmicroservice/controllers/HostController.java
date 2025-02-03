package com.peolly.paymentmicroservice.controllers;

import com.peolly.paymentmicroservice.dto.ApiResponse;
import com.peolly.paymentmicroservice.exceptions.IncorrectSearchPath;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class HostController {
    @Hidden
    @RequestMapping(value = "*")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }
}
