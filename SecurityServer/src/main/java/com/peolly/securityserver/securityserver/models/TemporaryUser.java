package com.peolly.securityserver.securityserver.models;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.UUID;

@RedisHash(value = "TemporaryUser", timeToLive = 60 * 15)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryUser implements Serializable {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;
    private String username;
    private String email;
    private String password;
}
