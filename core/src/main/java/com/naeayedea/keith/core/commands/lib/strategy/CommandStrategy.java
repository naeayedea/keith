package com.naeayedea.keith.core.commands.lib.strategy;

import com.naeayedea.keith.common.i18n.TranslationProvider;
import org.springframework.lang.NonNull;

public interface CommandStrategy {

    @NonNull
    TranslationProvider getTranslationProvider();
}
