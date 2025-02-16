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

    @Async
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

    @Async
    @SneakyThrows
    public void sendProductValidationErrorsEmail(String uploadLink, String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Issues Found in Uploaded Product File");
        helper.setTo(email);
        String emailContent = getProductValidationErrorsEmail(uploadLink);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getProductValidationErrorsEmail(String uploadLink) {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("uploadLink", uploadLink);
        configuration.getTemplate("product_validation_errors.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }

    @Async
    @SneakyThrows
    public void sendProductCreatedEmail(String productName, String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setSubject("Your product was successfully placed on Peolly Market!");
        helper.setTo(email);
        String emailContent = getProductCreatedEmail(productName);
        helper.setText(emailContent, true);
        mailSender.send(mimeMessage);
    }

    @SneakyThrows
    private String getProductCreatedEmail(String productName) {
        StringWriter writer = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("productName", productName);
        configuration.getTemplate("product_created.ftlh").process(model, writer);
        return writer.getBuffer().toString();
    }
}