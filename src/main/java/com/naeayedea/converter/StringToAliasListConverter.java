package com.naeayedea.converter;


import java.util.ArrayList;
import java.util.List;

public class StringToAliasListConverter {
    private StringToAliasListConverter() {
    }

    public static List<String> convert(String source, String separator) {
        List<String> aliases = new ArrayList<>(List.of(source.split(separator)));

        for (String alias : aliases) {
            if (alias.contains(" ")) {
                throw new RuntimeException("Aliases must not contain spaces! Offending alias: " + alias + ". Original string: " + source);
            }
        }

        return aliases;
    }

}
