package com.peolly.ordermicroservice.controllers;

import com.peolly.ordermicroservice.dto.AddToCartDto;
import com.peolly.ordermicroservice.dto.ApiResponse;
import com.peolly.ordermicroservice.dto.RemoveFromCartDto;
import com.peolly.ordermicroservice.exceptions.IncorrectSearchPathException;
import com.peolly.ordermicroservice.services.CartService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.UUID;

@Tag(name = "User cart page")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    @Value("${security.jwt.secret}")
    private String jwtSigningKey;

    private final CartService cartService;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPathException();
    }

//    @Operation(summary = "Get user cart: list of products and cart price")
//    @GetMapping()
//    public ResponseEntity<CartDto> getCartProducts(Principal actualUser) {
//        User requestUser = usersService.findByUsername(actualUser.getName());
//        UUID userId = requestUser.getId();
//        CartDto cartDto = cartService.listCartProducts(userId);
//        return new ResponseEntity<>(cartDto, HttpStatus.OK);
//    }

    @Operation(summary = "Add product to user cart")
    @PostMapping("/add-item")
    public ResponseEntity<?> addItemToCart(@RequestBody AddToCartDto addToCartDto,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(extractUserIdFromJwt(jwt));
        cartService.addToCart(addToCartDto.productId(), userId);
        return new ResponseEntity<>(new ApiResponse(true, "Added to Cart"), HttpStatus.OK);
    }

    @Operation(summary = "Remove product from user cart")
    @DeleteMapping("/remove-item")
    public ResponseEntity<?> removeItemFromCart(@RequestBody RemoveFromCartDto removeFromCartDto,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(extractUserIdFromJwt(jwt));
        cartService.removeFromCart(removeFromCartDto.productId(), userId);
        return new ResponseEntity<>(new ApiResponse(true, "Removed from Cart"), HttpStatus.OK);
    }

    public String extractUserIdFromJwt(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("id", String.class);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}