package com.peolly.catalogservice.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemValidationReport {
    private boolean isItemValid;
    private String incorrectData;
    private List<String> errorMessages;
}
