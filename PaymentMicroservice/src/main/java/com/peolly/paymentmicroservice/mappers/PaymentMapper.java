package com.peolly.paymentmicroservice.mappers;

import com.peolly.paymentmicroservice.dto.PaymentRequestDto;
import com.peolly.paymentmicroservice.models.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "paidAt", expression = "java(java.time.LocalDateTime.now())")
    Payment toEntity(PaymentRequestDto paymentRequestDto);

    default String map(CharSequence value) {
        return value == null ? null : value.toString();
    }
}
