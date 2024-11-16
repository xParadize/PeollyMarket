package com.peolly.securityserver.securityserver;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class NameGenerator {

    public String getImageName(String filename) {

        String packageOfSymbols = "-ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        String fileExtension = filename.substring(filename.lastIndexOf('.'));

        for (int i = 0; i < 40; i++) {
            int index = (int) (packageOfSymbols.length() * Math.random());
            sb.append(packageOfSymbols.charAt(index));
        }

        return sb + fileExtension;
    }

    public String getEchekName() {
        String packageOfSymbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        StringBuilder fileName = new StringBuilder();
        String fileExtension = ".pdf";

        for (int i = 0; i < 30; i++) {
            int index = (int) (packageOfSymbols.length() * Math.random());
            fileName.append(packageOfSymbols.charAt(index));
        }

        return fileName + fileExtension;
    }

    public String refreshTokenGenerator(String inputJwt) {
        // Генерируем случайную строку длиной 20 символов
        String randomString = generateRandomString(20);

        // Получаем последние 6 символов из Access токена
        String lastSixCharacters = getLastSixCharacters(inputJwt);

        // Склеиваем случайную строку и последние 6 символов из Access токена
        return randomString + lastSixCharacters;
    }

    // Генерируем случайную строку
    private static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }

    // Извлекаем последние 6 символов из Access токена
    private static String getLastSixCharacters(String accessToken) {
        return accessToken.substring(accessToken.length() - 6);
    }


}
