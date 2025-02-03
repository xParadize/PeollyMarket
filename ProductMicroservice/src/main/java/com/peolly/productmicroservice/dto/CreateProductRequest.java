package com.peolly.productmicroservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "Name can't be blank")
    @Size(min = 3, message = "Name can't be less than 3 characters")
    private String name;

    @NotBlank(message = "Description can't be blank")
    @Size(min = 10, message = "Description can't be less than 10 characters")
    private String description;

    @NotNull(message = "Company ID can't be null")
    @DecimalMin(value = "1", message = "Company ID must be greater than 1")
    private Long companyId;

    @NotNull(message = "Price can't be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Email should be like example@org.com")
    private String reportToEmail;
}
