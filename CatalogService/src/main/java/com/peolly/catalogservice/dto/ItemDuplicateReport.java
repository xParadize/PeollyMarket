package com.peolly.catalogservice.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDuplicateReport {
    private boolean isItemDuplicate;
    private String duplicateData;
    private String errorMessages;
}
