package com.peolly.catalogservice.dto;

import com.opencsv.bean.CsvBindByPosition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemCsvRepresentation {
    @CsvBindByPosition(position = 0)
    @NotBlank(message = "Name can't be blank.")
    @Size(min = 3, message = "Name can't be less than 3 characters.")
    private String name;

    @CsvBindByPosition(position = 1)
    @NotBlank(message = "Description can't be blank.")
    @Size(min = 10, message = "Description can't be less than 10 characters.")
    private String description;

    @CsvBindByPosition(position = 2)
    @NotBlank(message = "Image can't be blank.")
    private String image;

    @CsvBindByPosition(position = 3)
    @NotNull(message = "Category ID can't be null")
    private Long categoryId;

    @CsvBindByPosition(position = 4)
    @NotNull(message = "Price can't be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    public String toCsvString() {
        return name + "," + description + "," + image + "," + categoryId + "," + price;
    }
}