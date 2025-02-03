package com.peolly.productmicroservice.models;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductValidationReport {
    private boolean isProductValid;
    private String incorrectData;
    private List<String> errorMessages;
}
