package com.peolly.ordermicroservice.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartProductDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Double price;

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\", " +
                "\"name\": \"" + name + "\", " +
                "\"description\": \"" + description + "\", " +
                "\"image\": \"" + image + "\", " +
                "\"price\": \"" + price + "\", " +
                "}";
    }
}