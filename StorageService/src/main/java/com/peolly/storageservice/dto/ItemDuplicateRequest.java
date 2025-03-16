package com.peolly.storageservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ItemDuplicateRequest(
    @NotBlank String name,
    @NotBlank String description)
{
}