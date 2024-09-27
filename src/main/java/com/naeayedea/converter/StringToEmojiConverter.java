package com.naeayedea.converter;

import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class StringToEmojiConverter {

    private StringToEmojiConverter() {}

    public static List<UnicodeEmojiImpl> convertList(String source, String separator) {
        return Arrays.stream(source.split(separator)).map(UnicodeEmojiImpl::new).toList();
    }

    public static Emoji convert( String source) {
        return new UnicodeEmojiImpl(source);
    }
}
