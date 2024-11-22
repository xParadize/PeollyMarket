package com.peolly.paymentmicroservice.repositories;

import com.peolly.paymentmicroservice.models.UncheckedCard;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UncheckedCardRepository extends KeyValueRepository<UncheckedCard, String> {
}
