package com.peolly.ordermicroservice.controllers;

import com.peolly.ordermicroservice.dto.AddToCartDto;
import com.peolly.ordermicroservice.dto.ApiResponse;
import com.peolly.ordermicroservice.dto.CartDto;
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

    /**
     * Retrieves the products in the user's cart.
     *
     * @param authorizationHeader the Authorization header containing the JWT token.
     * @return ResponseEntity containing the user's cart as a CartDto object and HTTP status 200 (OK).
     */
    @Operation(summary = "Get user cart")
    @GetMapping()
    public ResponseEntity<?> getCartProducts(@RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(extractUserIdFromJwt(jwt));
        CartDto userCart = cartService.listCartProducts(userId);
        return new ResponseEntity<>(userCart, HttpStatus.OK);
    }

    /**
     * Adds a product to the user's cart.
     *
     * @param addToCartDto        the DTO containing product information to be added to the cart.
     * @param authorizationHeader the Authorization header containing the JWT token.
     * @return ResponseEntity with an ApiResponse indicating the success of the operation and HTTP status 200 (OK).
     */
    @Operation(summary = "Add product to user cart")
    @PostMapping("/add-item")
    public ResponseEntity<?> addItemToCart(@RequestBody AddToCartDto addToCartDto,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(extractUserIdFromJwt(jwt));
        cartService.addToCart(addToCartDto.productId(), userId);
        return new ResponseEntity<>(new ApiResponse(true, "Added to Cart"), HttpStatus.OK);
    }

    /**
     * Removes a product from the user's cart.
     *
     * @param removeFromCartDto   the DTO containing product information to be removed from the cart.
     * @param authorizationHeader the Authorization header containing the JWT token.
     * @return ResponseEntity with an ApiResponse indicating the success of the operation and HTTP status 204 (NO_CONTENT).
     */
    @Operation(summary = "Remove product from user cart")
    @DeleteMapping("/remove-item")
    public ResponseEntity<?> removeItemFromCart(@RequestBody RemoveFromCartDto removeFromCartDto,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        UUID userId = UUID.fromString(extractUserIdFromJwt(jwt));
        cartService.removeFromCart(removeFromCartDto.productId(), userId);
        return new ResponseEntity<>(new ApiResponse(true, "Removed from Cart"), HttpStatus.NO_CONTENT);
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param jwt the JWT token.
     * @return the user ID extracted from the JWT token.
     */
    public String extractUserIdFromJwt(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("id", String.class);
    }

    /**
     * Retrieves the signing key used to verify JWT tokens.
     *
     * @return the signing Key object used for HMAC SHA encryption.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}