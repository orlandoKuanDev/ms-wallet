package com.example.mswallet.util;

import java.util.Random;

public class VerificationCodeGenerator {

    private static final char[] characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final Random random = new Random(System.currentTimeMillis());

    /**
     *
     * @param length The total length (i.e. including the BIN) of the verification wallet code.
     * @return  A randomly generated, valid,verification wallet code.
     */
    public String generate(int length) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < builder.length(); i++) {
            String randomValue;
            if (random.nextInt(2) == 1) {
                randomValue = String.valueOf(random.nextInt(10));
            } else {
                randomValue = Character.toString(characters[random.nextInt(26)]);
            }
            builder.append(randomValue);
        }
        return builder.toString();
    }
}
