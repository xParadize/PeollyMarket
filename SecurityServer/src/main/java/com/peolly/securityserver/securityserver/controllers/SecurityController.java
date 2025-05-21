package com.peolly.securityserver.securityserver.controllers;

import com.peolly.securityserver.exceptions.IncorrectSearchPath;
import com.peolly.securityserver.exceptions.JwtTokenExpiredException;
import com.peolly.securityserver.securityserver.models.JwtAuthenticationResponse;
import com.peolly.securityserver.securityserver.models.RefreshTokenRequest;
import com.peolly.securityserver.securityserver.models.SignInRequest;
import com.peolly.securityserver.securityserver.models.SignUpRequest;
import com.peolly.securityserver.securityserver.services.AuthenticationService;
import com.peolly.securityserver.usermicroservice.dto.ApiResponse;
import com.peolly.securityserver.usermicroservice.dto.RoleUpdateRequest;
import com.peolly.securityserver.usermicroservice.services.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Security controller")
public class SecurityController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "Register user")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody @Valid SignUpRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        if (Objects.equals(request.getPassword(), request.getRepeatedPassword())) {
            try {
                authenticationService.createTempUser(request);
                return new ResponseEntity<>(new ApiResponse(true, "Please, Confirm Your Email"), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(new ApiResponse(false, "Registration error: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(new ApiResponse(false, "Passwords don't match"), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Authenticate user")
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request, BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            String errors = getFieldsErrors(bindingResult);
            return new ResponseEntity<>(new ApiResponse(false, errors), HttpStatus.BAD_REQUEST);
        }

        try {
            var response = authenticationService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (JwtTokenExpiredException e) {
            throw new JwtTokenExpiredException(e.getMessage());
        }
    }

    @Operation(summary = "Update refresh token and get a new one")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            JwtAuthenticationResponse response = authenticationService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Refresh token error: " + e.getMessage()));
        }
    }

    @Operation(summary = "Confirm account registration with a code from the mail")
    @GetMapping("/confirm-email/{code}")
    public ResponseEntity<?> confirmEmail(@PathVariable("code") String code) {
        String authResponse = authenticationService.confirmEmailToken(code);
        return ResponseEntity.ok(new ApiResponse(true, authResponse));
    }

    @Operation(summary = "Add or remove user role")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse> updateUserRole(@PathVariable("id") UUID userId, @RequestBody RoleUpdateRequest roleUpdateRequest) {
        userService.updateUserRole(userId, roleUpdateRequest.role(), roleUpdateRequest.isAdd());
        return ResponseEntity.ok(new ApiResponse(true, "Role modified successfully"));
    }

    private String getFieldsErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(error -> String.format("%s - %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return errorMessage;
    }
}