package com.peolly.catalogservice.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemValidationSummary {
    private List<ItemValidationReport> validationErrors;
    private List<ItemDuplicateReport> duplicateErrors;
}
