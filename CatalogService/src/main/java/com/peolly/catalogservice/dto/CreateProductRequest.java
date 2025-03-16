package com.peolly.catalogservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Название товара не может быть пустым")
    @Length(max = 200, message = "Название товара не может превышать 200 символов")
    @Pattern(
            regexp = "^[А-ЯA-Z][А-Яа-яA-Za-z0-9.,:;()\\-/&“”!?\\s]{1,199}$",
            message = "Название должно начинаться с большой буквы и содержать только разрешённые символы"
    )
    private String name;

    @NotBlank(message = "Описание товара не может быть пустым")
    @Length(max = 1000, message = "Описание товара не может превышать 1000 символов")
    private String description;

    @NotNull(message = "ID компании обязателен")
    @Min(value = 1, message = "ID компании должен быть положительным числом")
    private Long companyId;

    @NotNull(message = "ID категории обязателен")
    @Min(value = 1, message = "ID категории должен быть положительным числом")
    private Long categoryId;

    @URL(message = "Фото товара должно быть корректным URL-адресом")
    @NotBlank(message = "Фото товара обязательно")
    private String image;

    @NotNull(message = "Цена товара обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Некорректный формат цены")
    private Double price;

    @NotNull(message = "Скидка на товара обязательна")
    @DecimalMin(value = "0.00", message = "Скидка не может быть отрицательной")
    @DecimalMax(value = "100.00", message = "Скидка не может превышать 100%")
    private Double discount;

    @NotNull(message = "Количество товара обязательна")
    @Min(1)
    private int quantity;
}
