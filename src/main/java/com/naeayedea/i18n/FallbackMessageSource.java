package com.naeayedea.i18n;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

public class FallbackMessageSource implements MessageSource {

    private final MessageSource delegate;

    private final Locale fallbackLocale;

    public FallbackMessageSource(MessageSource delegate, Locale fallbackLocale) {
        this.delegate = delegate;
        this.fallbackLocale = fallbackLocale;
    }

    public FallbackMessageSource(MessageSource delegate) {
        this.delegate = delegate;

        this.fallbackLocale = Locale.getDefault();
    }

    @Override
    public String getMessage(@NotNull String code, Object[] args, String defaultMessage, @NotNull Locale locale) {
        try {
            return delegate.getMessage(code, args, defaultMessage, locale);
        } catch (NoSuchMessageException ex) {
            return delegate.getMessage(code, args, defaultMessage, fallbackLocale);
        }
    }

    @NotNull
    @Override
    public String getMessage(@NotNull String code, Object[] args, @NotNull Locale locale) throws NoSuchMessageException {
        try {
            return delegate.getMessage(code, args, locale);
        } catch (NoSuchMessageException ex) {
            return delegate.getMessage(code, args, fallbackLocale);
        }
    }

    @NotNull
    @Override
    public String getMessage(@NotNull MessageSourceResolvable resolvable, @NotNull Locale locale) throws NoSuchMessageException {
        try {
            return delegate.getMessage(resolvable, locale);
        } catch (NoSuchMessageException ex) {
            return delegate.getMessage(resolvable, fallbackLocale);
        }
    }
}
