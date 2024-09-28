package com.naeayedea.i18n;

import net.dv8tion.jda.api.interactions.DiscordLocale;

public interface LocalizationRetriever {

    String getLocalization(DiscordLocale discordLocale);
}
