package com.naeayedea.keith.common.util;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Utilities {

    public static Color getColorFromString(String string) {
        String stringHash;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(string.getBytes());
            stringHash = new String(messageDigest.digest());

        } catch (NoSuchAlgorithmException e) {
            //if for some reason SHA-256 was to disappear, just revert to regular old string.getHash()
            stringHash = string;
        }
        int pv = 0xFFFFFF & stringHash.hashCode();
        int R, G, B;
        R = pv & 255;
        G = (pv >> 8) & 255;
        B = (pv >> 16) & 255;
        return new Color(R, G, B);
    }

    public static Color getRandomColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Color(random.nextInt(0, 255 + 1), random.nextInt(0, 255 + 1), random.nextInt(0, 255 + 1));
    }

    //rebuilds a string list into a "sentence" by appending spaces
    public static String stringListToString(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String string : list) {
            result.append(string).append(" ");
        }
        return result.toString().trim();
    }

    public static String truncateString(String string, int length) {
        if (length > 0) {
            String format = "%-" + length + "s";
            String result = String.format(format, string);
            if (string.length() > length) {
                result = result.substring(0, length - 2) + "..";
            }
            return result;
        }
        return "";
    }

}
