package com.peolly.catalogservice.controllers;

import com.peolly.catalogservice.dto.ApiResponse;
import com.peolly.catalogservice.dto.CreateProductRequest;
import com.peolly.catalogservice.exceptions.IncorrectSearchPath;
import com.peolly.catalogservice.services.CatalogService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    @Value("${security.jwt.secret}")
    private String jwtSigningKey;

    private final CatalogService catalogService;

    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Shows one product by its id")
    @GetMapping("/item/{id}")
    public ResponseEntity<?> getItemById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(catalogService.findItemById(id), HttpStatus.OK);
    }

    @PostMapping(value = "/create-product")
    public ResponseEntity<ApiResponse> createProduct(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody @Valid CreateProductRequest createProductRequest,
                                                     BindingResult bindingResult) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        String creatorEmail = extractUserEmailFromJwt(jwt);

        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        boolean isDuplicate = catalogService.isProductDuplicate(createProductRequest);
        if (isDuplicate) {
            return new ResponseEntity<>(new ApiResponse(false, "Product contains duplicate data. Please fix it and make request again."), HttpStatus.BAD_REQUEST);
        }

        catalogService.saveCatalogItem(createProductRequest, creatorEmail);
        return new ResponseEntity<>(new ApiResponse(true, "Product added. You will receive an email notification with more information."), HttpStatus.OK);
    }

    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("\n"));
        return errorMessage;
    }

    public String extractUserEmailFromJwt(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("email", String.class);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
