package com.peolly.paymentmicroservice.dto;

import com.peolly.paymentmicroservice.models.UncheckedCard;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface UncheckedCardMapper {
    UncheckedCard toEntity(CardDto dto);
}
