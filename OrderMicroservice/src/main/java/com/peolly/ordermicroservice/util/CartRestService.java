package com.peolly.ordermicroservice.util;

import com.peolly.ordermicroservice.exceptions.ItemNotFoundException;
import com.peolly.ordermicroservice.external.ItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CartRestService {
    private final RestClient restClient = RestClient.create();

    public ItemDto getItemInfo(Long itemId) {
        ItemDto itemDto = restClient.get()
                .uri("http://localhost:8030/api/v1/catalog/item/{id}", itemId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ItemNotFoundException("Item not found.");
                })
                .body(ItemDto.class);
        return itemDto;
    }
}

