package com.peolly.securityserver.usermicroservice.services;

import com.peolly.utilservice.events.PaymentMethodWasNotAddedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InvalidCardDataProcessor {
    public List<String> getCardInvalidFields(PaymentMethodWasNotAddedEvent event) {
        List<String> fields = new ArrayList<>();
        System.out.println(event.toString());
        if (!event.isExpirationFieldsValid()) {
            fields.add("Expiration Fields Invalid");
        }
        if (!event.isBalanceSufficient()) {
            fields.add("Insufficient Balance");
        }
        if (!event.isCardNotInUse()) {
            fields.add("Card Already in Use");
        }
        return fields;
    }
}
