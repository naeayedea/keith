package com.naeayedea.keith.platform.discord.command.general.ping;

import com.naeayedea.keith.core.commands.drivers.general.ping.PingCommandStrategy;
import com.naeayedea.keith.common.i18n.TranslationProvider;
import org.springframework.lang.NonNull;

public class DiscordPingCommandStrategy implements PingCommandStrategy {

    private final TranslationProvider translationProvider;

    public DiscordPingCommandStrategy(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
    }

    @Override
    @NonNull
    public TranslationProvider getTranslationProvider() {
        return translationProvider;
    }
}
