package com.peolly.securityserver.securityserver.services;

import com.peolly.securityserver.securityserver.models.JwtAuthenticationResponse;
import com.peolly.securityserver.securityserver.util.NameGenerator;
import com.peolly.securityserver.securityserver.models.RefreshTokenRequest;
import com.peolly.securityserver.securityserver.models.RefreshToken;
import com.peolly.securityserver.securityserver.models.SignInRequest;
import com.peolly.securityserver.securityserver.models.SignUpRequest;
import com.peolly.securityserver.securityserver.models.TemporaryUser;
import com.peolly.securityserver.usermicroservice.enums.UserRole;
import com.peolly.securityserver.usermicroservice.model.User;
import com.peolly.securityserver.usermicroservice.services.AuthDeviceInfoService;
import com.peolly.securityserver.usermicroservice.services.UserService;
import com.peolly.utilservice.events.SendUserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TempUserService tempUserService;
//    private final EmailConfirmationService emailConfirmationService;
//    private final MailService mailService;
    private final RefreshTokenService refreshTokenService;
//    private final NotificationService notificationService;
    private final NameGenerator nameGenerator;
    private final AuthDeviceInfoService authDeviceInfoService;

    private final KafkaTemplate<String, SendUserCreatedEvent> sendUserCreatedEmailEvent;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void createTempUser(SignUpRequest request) throws ExecutionException, InterruptedException {
        var tempUser = TemporaryUser.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        tempUserService.create(tempUser);
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

        // notificationService.sendNotification(userToSave, NotificationType.REGISTRATION_NOTIFICATION);

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
    public JwtAuthenticationResponse signIn(SignInRequest request) {


        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtService.generateToken(userDetails);

        User tempUser = userService.findByUsername(request.getUsername());
        RefreshToken userRefreshToken = refreshTokenService.findRefreshTokenByUserId(tempUser.getId());
        var refreshToken = userRefreshToken.getToken();

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();

        authDeviceInfoService.saveSession(tempUser);

        return response;
    }

    @Transactional
    public String confirmEmailToken(String token) throws ExecutionException, InterruptedException {

        TemporaryUser temporaryUser = tempUserService.findTempUserById(UUID.fromString(token))
                .orElse(null);

        if (temporaryUser == null) {
            return "Email link has been expired";
        }

        var registeredUserData = SendUserCreatedEvent.builder()
                .userToken(token)
                .email(temporaryUser.getEmail())
                .username(temporaryUser.getUsername())
                .build();

        ProducerRecord<String, SendUserCreatedEvent> record = new ProducerRecord<>(
                "send-user-created-email",
                token,
                registeredUserData
        );
        SendResult<String, SendUserCreatedEvent> result = sendUserCreatedEmailEvent
                .send(record).get();
        LOGGER.info("Sent event to topic 'send-user-created-email': {}", result);

        JwtAuthenticationResponse response = enableUser(temporaryUser);

        return response.getAccessToken();
    }

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
