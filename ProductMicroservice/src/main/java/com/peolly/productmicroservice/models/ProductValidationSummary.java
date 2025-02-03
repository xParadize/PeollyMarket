package com.peolly.productmicroservice.models;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductValidationSummary {
    private List<ProductValidationReport> validationErrors;
    private List<ProductsDuplicateReport> duplicateErrors;
}
