package com.peolly.catalogservice.controllers;

import com.peolly.catalogservice.dto.ApiResponse;
import com.peolly.catalogservice.dto.CreateProductRequest;
import com.peolly.catalogservice.exceptions.IncorrectSearchPath;
import com.peolly.catalogservice.services.CatalogService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Catalog controller")
public class CatalogController {

    @Value("${security.jwt.secret}")
    private String jwtSigningKey;

    private final CatalogService catalogService;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Get item by ID")
    @GetMapping("/items/{id}")
    public ResponseEntity<?> getItemById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(catalogService.findItemById(id), HttpStatus.OK);
    }

    @Operation(summary = "Add one item")
    @PostMapping(value = "/items")
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

    @Operation(summary = "Add many items from .csv file")
    @PostMapping(value = "/items")
    public ResponseEntity<ApiResponse> createProducts() {
        // TODO: вернуть метод из ProductService
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
