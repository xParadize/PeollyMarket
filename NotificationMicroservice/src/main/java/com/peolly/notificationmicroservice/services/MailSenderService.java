package com.peolly.notificationmicroservice.services;

import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final Configuration configuration;
    private final JavaMailSender mailSender;

    @Async
    @SneakyThrows
    public void sendVerifyEmail(String email, UUID confirmationCode) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("To continue please confirm your email address");
        helper.setTo(email);
        String emailContent = getVerifyEmail(confirmationCode);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getVerifyEmail(UUID confirmationCode) {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("code", confirmationCode);
        configuration.getTemplate("email_confirm.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendRegistrationEmail(String email, String username) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject(String.format("Thank you for registration, %s!", username));
        helper.setTo(email);
        String emailContent = getRegistrationEmailContent(username);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getRegistrationEmailContent(String username) {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("name", username);
        configuration.getTemplate("register.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @SneakyThrows
    public void sendCreditCartLinkedEmail(String email, String cardNumber, boolean isCardDataValid) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(email);

        String emailContent;
        if (isCardDataValid) {
            // TODO: сделать динамические хедер и месседж из сервиса чтобы одно и то же сообщение
            //  разлеталось с одинаковым контентом по всем местам
            //  и типа через модель тут добавить
            emailContent = getCreditCartLinkedEmailContent(cardNumber);
        } else {
            emailContent = getCreditCartWasNotLinkedEmailContent();
        }
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getCreditCartLinkedEmailContent(String cardNumber) {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("card_number", "*" + cardNumber.substring(cardNumber.length() - 4));
        configuration.getTemplate("credit_card_linked.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @SneakyThrows
    private String getCreditCartWasNotLinkedEmailContent() {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        configuration.getTemplate("credit_card_was_not_linked.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

//    @Async
//    @SneakyThrows
//    public void sendTicketCreated(User user) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//        helper.setSubject("Company ticket status: sent for processing");
//        helper.setTo(user.getEmail());
//        String emailContent = getTicketCreatedEmail(user);
//        helper.setText(emailContent, true);
//        mailSender.send(mimeMessage);
//    }
//
//    @SneakyThrows
//    private String getTicketCreatedEmail(User user) {
//        StringWriter writer = new StringWriter();
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", user.getUsername());
//        configuration.getTemplate("company_ticket_created.ftlh").process(model, writer);
//        return writer.getBuffer().toString();
//    }
//
//    @Async
//    @SneakyThrows
//    public void sendTicketRejected(User user, String message) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//        helper.setSubject("Company ticket status: rejected");
//        helper.setTo(user.getEmail());
//        String emailContent = getTicketRejected(user, message);
//        helper.setText(emailContent, true);
//        mailSender.send(mimeMessage);
//    }
//
//    @SneakyThrows
//    private String getTicketRejected(User user, String message) {
//        StringWriter writer = new StringWriter();
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", user.getUsername());
//        model.put("message", message);
//        configuration.getTemplate("company_ticket_rejected.ftlh").process(model, writer);
//        return writer.getBuffer().toString();
//    }
//
//    @Async
//    @SneakyThrows
//    public void sendTicketApproved(User user) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//        helper.setSubject("Company ticket status: approved");
//        helper.setTo(user.getEmail());
//        String emailContent = getTicketApproved(user);
//        helper.setText(emailContent, true);
//        mailSender.send(mimeMessage);
//    }
//
//    @SneakyThrows
//    private String getTicketApproved(User user) {
//        StringWriter writer = new StringWriter();
//        Map<String, Object> model = new HashMap<>();
//        model.put("name", user.getUsername());
//        configuration.getTemplate("company_ticket_approved.ftlh").process(model, writer);
//        return writer.getBuffer().toString();
//    }
//
//    @Async
//    @SneakyThrows
//    public void sendOrderCheck(User user, String checkPath) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//        helper.setSubject("E-check");
//        helper.setTo(user.getEmail());
//        String emailContent = getSendOrderCheck(checkPath);
//        helper.setText(emailContent, true);
//        mailSender.send(mimeMessage);
//    }
//
//    @SneakyThrows
//    private String getSendOrderCheck(String checkNumber) {
//        StringWriter writer = new StringWriter();
//        Map<String, Object> model = new HashMap<>();
//        model.put("checkNumber", checkNumber);
//        configuration.getTemplate("e-check.ftlh").process(model, writer);
//        return writer.getBuffer().toString();
//    }
}