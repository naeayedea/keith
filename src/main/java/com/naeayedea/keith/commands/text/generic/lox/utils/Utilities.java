package com.naeayedea.keith.commands.text.generic.lox.utils;

public class Utilities {

    public static String stringify(Object object) {
        if (object == null) return "nil";
        String text = object.toString();
        if (object instanceof Double) {
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return text;
    }
}
