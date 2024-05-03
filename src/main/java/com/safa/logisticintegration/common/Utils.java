package com.safa.logisticintegration.common;

import com.safa.logisticintegration.exception.IntegrationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Utils {

    public static String generateSHA512Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashedBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to a hexadecimal string representation
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IntegrationException("Unable to generate SHA-512 hash", e);
        }
    }

    /**
     * convert date with time (Date) to LocalDate (without time)
     *
     * @param date
     * @return
     */
    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
    }

}
