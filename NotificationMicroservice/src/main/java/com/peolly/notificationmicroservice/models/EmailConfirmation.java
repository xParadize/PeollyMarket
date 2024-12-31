package com.peolly.notificationmicroservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_confirmation")
public class EmailConfirmation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String token;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime confirmedAt;
}
