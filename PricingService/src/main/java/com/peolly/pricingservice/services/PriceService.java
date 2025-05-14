package com.peolly.pricingservice.services;

import com.peolly.pricingservice.external.PricesRefreshResponse;
import com.peolly.pricingservice.mappers.PriceMapper;
import com.peolly.pricingservice.models.Price;
import com.peolly.pricingservice.repositories.PriceRepository;
import com.peolly.schemaregistry.CreateItemEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final PriceRepository priceRepository;
    private final PriceMapper priceMapper;

    @Transactional(readOnly = true)
    public List<PricesRefreshResponse> getPricesWithDiscount(List<Long> itemIds) {
        return itemIds.stream()
                .map(itemId -> new PricesRefreshResponse(itemId, priceRepository.getUpdatedPriceForItem(itemId)))
                .collect(Collectors.toList());
    }

    @Transactional
    @KafkaListener(topics = "item-created", groupId = "org-deli-queuing-pricing")
    public void consumeCreateItemEvent(CreateItemEvent event) {
        Price price = priceMapper.toEntity(event);
        savePrice(price);
    }

    @Transactional
    public void savePrice(Price price) {
        priceRepository.save(price);
    }
}
