package com.naeayedea.keith.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TranslationProvider {

    public static final String NAME_TRANSLATION_SUFFIX = "name";

    public static final String DESCRIPTION_TRANSLATION_SUFFIX = "desc.slash";

    public static final String MESSAGE_COMMAND_DESCRIPTION_TRANSLATION_SUFFIX = "desc.message";

    public static final String USER_COMMAND_DESCRIPTION_TRANSLATION_SUFFIX = "desc.user";

    private final MessageSource messageSource;

    public TranslationProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getTranslation(String key, Locale locale) {
        return getTranslation(key, new Object[]{}, locale);
    }

    public String getTranslation(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }

    public String getTranslationKey(String prefix, String name, String target) {
        return "translation.i18n.commands." + (prefix.isEmpty() ? "" : prefix + ".") + name + "." + target;
    }
}
