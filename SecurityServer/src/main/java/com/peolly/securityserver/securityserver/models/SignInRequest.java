package com.peolly.securityserver.securityserver.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Auth request")
public class SignInRequest {

    @Schema(description = "Username", example = "Ogyzok")
    @NotEmpty(message = "This field should not be empty")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Only alphabet symbols are allowed")
    @Size(min = 3, message = "Name can't be less than 3 symbols")
    private String username;

    @Schema(description = "Password", example = "bigBoy1236969")
    @Size(min = 6, message = "Password can't be less than 6 symbols")
    private String password;
}
