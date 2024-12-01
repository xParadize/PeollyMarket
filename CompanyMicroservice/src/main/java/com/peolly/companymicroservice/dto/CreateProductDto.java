package com.peolly.companymicroservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateProductDto {

    @Schema(description = "Product name", example = "Coca-Cola")
    @NotEmpty(message = "This field should not be empty")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Only alphabet symbols are allowed")
    @Size(min = 3, message = "Product name can't be less than 3 symbols")
    private String name;

    @Schema(description = "Product description", example = "Carbonated drink with sugar")
    @NotEmpty(message = "This field should not be empty")
    @Size(min = 10, message = "Product description can't be less than 10 symbols")
    private String description;

    @Schema(description = "Company ID", example = "123")
    @NotNull(message = "This field should not be empty")
    @Digits(integer = 10, fraction = 0, message = "Incorrect companyID format")
    private Long companyId;

    @Schema(description = "Product price", example = "911.99")
    @NotNull(message = "This field should not be empty")
    @Digits(integer = 10, fraction = 2, message = "Incorrect product price format")
    @Min(value = 1, message = "Product price can't be less than 1")
    private Double price;
}
