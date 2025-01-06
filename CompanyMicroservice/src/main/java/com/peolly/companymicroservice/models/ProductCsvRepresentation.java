package com.peolly.companymicroservice.models;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ProductCsvRepresentation {
    @NotBlank(message = "Name can't be blank.")
    @Size(min = 3, message = "Name can't be less than 3 characters.")
    private String name;

    @NotBlank(message = "Description can't be blank.")
    @Size(min = 10, message = "Description can't be less than 10 characters.")
    private String description;

    @NotNull(message = "Company ID can't be null.")
    @Pattern(regexp = "^[1-9]+[0-9]*$", message = "Company ID must be greater than 0.")
    private Long companyId;

    @NotNull(message = "Price can't be null.")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.")
    private Double price;
}