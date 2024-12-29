package com.peolly.services;

import org.springframework.stereotype.Service;

@Service
public class TelegramSenderService {
    public void sendCardLinkedMessage(String cardNumber, boolean isCardValid) {
        if (isCardValid) {
            System.out.printf("TG Bot: Your card %s added to payment methods.", cardNumber);
        } else {
            System.out.println("TG Bot: Your card wasn't added - check data and fill again.");
        }
    }
}
