package com.peolly.securityserver.securityserver.tempregistration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.UUID;

@RedisHash(value = "TemporaryUser", timeToLive = 900) // 15 mins
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryUser implements Serializable {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @NotEmpty(message = "This field should not be empty")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Only alphabet symbols are allowed")
    @Size(min = 3, message = "Name can't be less than 3 symbols")
    private String username;

    @Email(message = "Incorrect email")
    private String email;

    @Size(min = 6, message = "Password can't be less than 6 symbols")
    private String password;
}
