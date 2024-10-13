package com.naeayedea.keith.converter;


import java.util.ArrayList;
import java.util.List;

public class StringToAliasListConverter {
    private StringToAliasListConverter() {
    }

    public static List<String> convert(String source, String separator) {
        if (source == null || source.trim().isEmpty())
            return new ArrayList<>();

        if (separator == null)
            return new ArrayList<>(List.of(source));

        List<String> aliases = new ArrayList<>(List.of(source.trim().split(separator)));

        for (String alias : aliases) {
            if (alias.contains(" ")) {
                throw new RuntimeException("Aliases must not contain spaces! Offending alias: " + alias + ". Original string: " + source);
            }
        }

        return aliases;
    }

}
