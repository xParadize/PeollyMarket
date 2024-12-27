package com.peolly.paymentmicroservice.dto;

import com.peolly.paymentmicroservice.models.Card;
import com.peolly.utilservice.events.SavePaymentMethodEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    @Mapping(target = "userId", ignore = true)
    Card toEntity(CardDto dto);
    CardDto toDto(Card card);
    CardDto toDto(SavePaymentMethodEvent event);
}
