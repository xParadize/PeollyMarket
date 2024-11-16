package com.peolly.securityserver.securityserver;

import com.peolly.securityserver.ApiResponse;
import com.peolly.securityserver.usermicroservice.exceptions.IncorrectSearchPath;
import com.peolly.securityserver.usermicroservice.exceptions.JwtTokenExpiredException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class SecurityController {

    private final AuthenticationService authenticationService;

    @Hidden
    @RequestMapping(value = "/**")
    public ResponseEntity<ApiResponse> handleNotFound() {
        throw new IncorrectSearchPath();
    }

    @Operation(summary = "User registration")
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request) {

        if (Objects.equals(request.getPassword(), request.getRepeatedPassword())) {
            try {
                authenticationService.createTempUser(request);
                return ResponseEntity.ok(new ApiResponse(true, "Confirm email"));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Registration error: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Passwords don't match"));
        }
    }

    @Operation(summary = "User authentication")
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request) {
        try {
            var response = authenticationService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (JwtTokenExpiredException e) {
            throw new JwtTokenExpiredException(e.getMessage());
        }
    }

    @Operation(summary = "User authentication")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        try {
            JwtAuthenticationResponse response = authenticationService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Refresh: " + e.getMessage()));
        }
    }

    @GetMapping("/confirm-email/{confirmation_code}")
    public ResponseEntity<?> confirmEmail(@PathVariable("confirmation_code") String uuid) throws ExecutionException, InterruptedException {
        String authResponse = authenticationService.confirmEmailToken(uuid);
        if (authResponse.equals("expired")) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found or token has expired"));
        }
        return ResponseEntity.ok(new ApiResponse(true, authResponse));
    }
}