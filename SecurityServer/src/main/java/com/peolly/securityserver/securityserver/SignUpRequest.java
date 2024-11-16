package com.peolly.securityserver.securityserver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Registration request")
public class SignUpRequest {

    @Schema(description = "Username", example = "Ogyzok")
    @NotEmpty(message = "This field should not be empty")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Only alphabet symbols are allowed")
    @Size(min = 3, message = "Name can't be less than 3 symbols")
    private String username;

    @Schema(description = "Email address", example = "ogyzok@gmail.com")
    @Size(min = 5, max = 255, message = "Address should contain from 5 to 255 symbols")
    @NotBlank(message = "This field should not be empty")
    @Email(message = "Email should be in format user@example.com")
    private String email;

    @Schema(description = "Password", example = "bigBoy1236969")
    @Size(min = 6, message = "Password can't be less than 6 symbols")
    private String password;

    @Schema(description = "Repeated password", example = "bigBoy1236969")
    @Size(min = 6, message = "Password can't be less than 6 symbols")
    private String repeatedPassword;
}
