//package com.peolly.paymentmicroservice.services;
//
//import com.peolly.paymentmicroservice.models.Card;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//
//import java.io.FileNotFoundException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentService {
//
//    private final CardService cardService;
//    private final CardDataValidator cardDataValidator;
//    private final UserService usersService;
//    private final CartService cartService;
//    private final MailService mailService;
//    private final PaymentRepository paymentRepository;
//    private final CardRepository cardRepository;
//    private final GeneratePdf generatePdf;
//
//    @Transactional
//    public void performPayment(UUID userId, String cardNumber) throws FileNotFoundException {
//        subtractMoney(userId, cardNumber);
//        String checkPath = generatePdf.generateEcheck(userId);
//        sendEmail(userId, checkPath);
//        Double totalCost = getTotalCost(userId);
//        int totalItems = getTotalItems(userId);
//        deleteCartAfterPayment(userId);
//        setPaymentStatus(userId, cardNumber, totalCost, totalItems);
//    }
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
//
//        Card cardToCheck = cardService.getUserCardByCardNumber(cardNumber);
//
//
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
//    private Double getTotalCost(UUID userId) {
//        CartDto cartData = cartService.listCartProducts(userId);
//        return cartData.getTotalCost();
//    }
//
//    private int getTotalItems(UUID userId) {
//        CartDto cartData = cartService.listCartProducts(userId);
//        return cartData.getTotalItems();
//    }
//
//    @Transactional
//    public void subtractMoney(UUID userId, String cardNumber) {
//        Double moneyToPay = getTotalCost(userId);
//        cardRepository.subtractMoneyForPurchase(cardNumber, moneyToPay);
//    }
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
//    @Transactional
//    public void setPaymentStatus(UUID userId, String cardNumber, Double totalCost, int totalItems) {
//        Payment payment = Payment.builder()
//                .userId(userId)
//                .cardNumber(cardNumber)
//                .totalPrice(totalCost)
//                .totalItems(totalItems)
//                .paidAt(LocalDateTime.now())
//                .build();
//        paymentRepository.save(payment);
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
//}
