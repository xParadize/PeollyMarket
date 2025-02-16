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
public class UpdateProductRequest {
    @NotBlank(message = "Name can't be blank")
    @Size(min = 3, message = "Name can't be less than 3 characters")
    private String name;

    @NotBlank(message = "Image can't be blank")
    private String image;

    @NotBlank(message = "Description can't be blank")
    @Size(min = 10, message = "Description can't be less than 10 characters")
    private String description;

    @NotNull(message = "Price can't be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;
}
