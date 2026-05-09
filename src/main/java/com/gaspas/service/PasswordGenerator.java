package com.gaspas.service;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length, boolean useUppercase, boolean useDigits, boolean useSymbols) {
        String chars = LOWERCASE;
        if (useUppercase) chars += UPPERCASE;
        if (useDigits) chars += DIGITS;
        if (useSymbols) chars += SYMBOLS;

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    public String generateStrong(int length) {
        String chars = LOWERCASE + UPPERCASE + DIGITS + SYMBOLS;
        StringBuilder password = new StringBuilder(length);
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        for (int i = 4; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        char[] charsArr = password.toString().toCharArray();
        for (int i = charsArr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = charsArr[i];
            charsArr[i] = charsArr[j];
            charsArr[j] = tmp;
        }
        return new String(charsArr);
    }
}
