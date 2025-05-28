package com.peolly.securityserver.securityserver.services;

import com.peolly.securityserver.exceptions.EmailConfirmationTokenExpiredException;
import com.peolly.securityserver.kafka.SecurityKafkaProducer;
import com.peolly.securityserver.securityserver.models.*;
import com.peolly.securityserver.securityserver.util.NameGenerator;
import com.peolly.securityserver.usermicroservice.enums.UserRole;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.services.AuthDeviceInfoService;
import com.peolly.securityserver.usermicroservice.services.UserService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TempUserService tempUserService;
    private final RefreshTokenService refreshTokenService;
    private final NameGenerator nameGenerator;
    private final AuthDeviceInfoService authDeviceInfoService;
    private final SecurityKafkaProducer securityKafkaProducer;

    private final MeterRegistry meterRegistry;
    private AtomicInteger signIns;

    @PostConstruct
    private void initMetrics() {
        this.signIns = new AtomicInteger(0);
        signIns = meterRegistry.gauge("sign_ins_total",
                Tags.of("type", "success"),
                signIns);
    }

    public void createTempUser(SignUpRequest request) {
        var tempUser = TemporaryUser.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        tempUserService.createTempUser(tempUser);
    }

    @Transactional
    public JwtAuthenticationResponse enableUser(TemporaryUser temporaryUser) {
        User userToSave = convertTempUserToUser(temporaryUser);
        userService.saveUser(userToSave);
        tempUserService.deleteTempUserById(temporaryUser.getId());

        var jwt = jwtService.generateToken(userToSave);
        var refreshToken = nameGenerator.refreshTokenGenerator(jwt);
        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();

        RefreshToken refreshTokenToSave = RefreshToken.builder()
                .userId(userToSave.getId())
                .token(refreshToken)
                .build();

        refreshTokenService.saveRefreshToken(refreshTokenToSave);
        return response;
    }

    private User convertTempUserToUser(TemporaryUser request) {
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(new HashSet<>(Arrays.asList(UserRole.ROLE_USER, UserRole.ROLE_VERIFIED_EMAIL)))
                .build();

        return user;
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Timed
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);
        User tempUser = userService.findByUsername(request.getUsername());
        RefreshToken userRefreshToken = refreshTokenService.findRefreshTokenByUserId(tempUser.getId());
        var refreshToken = userRefreshToken.getToken();

        signIns.incrementAndGet();

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();

        authDeviceInfoService.saveSession(tempUser);
        return response;
    }

    @Transactional
    public String confirmEmailToken(String token) {
        TemporaryUser temporaryUser = tempUserService.findTempUserById(UUID.fromString(token))
                .orElseThrow(EmailConfirmationTokenExpiredException::new);
         securityKafkaProducer.sendEmailConfirmed(token, temporaryUser);
        JwtAuthenticationResponse response = enableUser(temporaryUser);
        return response.getAccessToken();
    }

    @Transactional
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken oldRefreshToken = refreshTokenService.findRefreshTokenByToken(refreshTokenRequest.getRefreshToken());
        User user = userService.findById(oldRefreshToken.getUserId());

        var jwt = jwtService.generateToken(user);
        var newRefreshToken = nameGenerator.refreshTokenGenerator(jwt);
        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(newRefreshToken)
                .build();
        refreshTokenService.changeRefreshToken(refreshTokenRequest.getRefreshToken(), newRefreshToken);
        return response;
    }
}
