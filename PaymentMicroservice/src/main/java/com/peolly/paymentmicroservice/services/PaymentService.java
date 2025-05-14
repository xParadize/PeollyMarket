package com.peolly.paymentmicroservice.services;

import com.peolly.paymentmicroservice.dto.PaymentRequestDto;
import com.peolly.paymentmicroservice.mappers.PaymentMapper;
import com.peolly.paymentmicroservice.models.Payment;
import com.peolly.paymentmicroservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CardService cardService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public boolean isCardValidForPayment(String cardNumber, UUID userId, double totalCost) {
        boolean cardValidForPayment = cardService.isCardValidForPayment(cardNumber, userId, totalCost);
        return cardValidForPayment;
    }

    @Transactional
    public void performPayment(PaymentRequestDto paymentRequestDto) {
        subtractMoney(paymentRequestDto.cardNumber(), paymentRequestDto.totalCost());
        savePayment(paymentRequestDto);
    }

    @Transactional
    public void subtractMoney(String cardNumber, double totalCost) {
        cardService.takeMoneyFromCard(cardNumber, totalCost);
    }

    @Transactional
    public void savePayment(PaymentRequestDto paymentRequestDto) {
        Payment payment = paymentMapper.toEntity(paymentRequestDto);
        paymentRepository.save(payment);
    }

//
//    public boolean isPaymentCredentialsOk(UUID userId, String cardNumber) {
//        return isCardLinkedToUser(userId, cardNumber) && isCardNotExpired(cardNumber);
//    }
//
//    private boolean isCardLinkedToUser(UUID userId, String cardNumber) {
//        return (cardService.isCardBelongToUser(cardNumber, userId));
//    }
//
//    private boolean isCardNotExpired(String cardNumber) {
//        Card cardToCheck = cardService.getUserCardByCardNumber(cardNumber);
//        return cardDataValidator.isCardValid();
//    }
//
//    public boolean isEnoughMoney(String cardNumber, UUID userId) {
//        Card cardToCheck = cardService.getUserCardByCardNumber(cardNumber);
//        Double cardBalance = cardToCheck.getAvailableMoney();
//        Double totalCost = getTotalCost(userId);
//        return cardBalance >= totalCost;
//    }
//

//
//    private int getTotalItems(UUID userId) {
//        CartDto cartData = cartService.listCartProducts(userId);
//        return cartData.getTotalItems();
//    }
//

//
//    public void sendEmail(UUID userId, String checkPath) {
//        User user = usersService.findById(userId);
//        mailService.sendOrderCheck(user, checkPath);
//    }
//
//    @Transactional
//    public void deleteCartAfterPayment(UUID userId) {
//        String key = "Cart:" + userId;
//        Jedis jedis = new Jedis("localhost", 6379);
//        jedis.del(key);
//        jedis.close();
//    }
//


//    @Transactional(readOnly = true)
//    public int getUserGoodsPurchased(User user) {
//        List<Payment> payments = paymentRepository.findAllByUserId(user.getId());
//        int totalItems = 0;
//        for (Payment p : payments) {
//            totalItems += p.getTotalItems();
//        }
//        return totalItems;
//    }
//
//    @Transactional(readOnly = true)
//    public Double getUserMoneySpent(User user) {
//        List<Payment> payments = paymentRepository.findAllByUserId(user.getId());
//        Double totalPrice = 0.0;
//        for (Payment p : payments) {
//            totalPrice += p.getTotalPrice();
//        }
//        return totalPrice;
//    }
}
