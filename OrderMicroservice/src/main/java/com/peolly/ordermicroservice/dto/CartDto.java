package com.peolly.ordermicroservice.dto;

import com.peolly.ordermicroservice.models.Cart;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CartDto {
    private Cart cart;
    private Double totalCost;
    private Double priceWithDiscount;
    private int itemsCount;
}
