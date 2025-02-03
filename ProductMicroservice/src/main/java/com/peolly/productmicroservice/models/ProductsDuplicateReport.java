package com.peolly.productmicroservice.models;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductsDuplicateReport {
    private boolean isProductDuplicate;
    private String duplicateData;
    private String errorMessages;
}
