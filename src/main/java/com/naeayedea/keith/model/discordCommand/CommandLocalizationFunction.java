package com.naeayedea.keith.model.discordCommand;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommandLocalizationFunction implements LocalizationFunction {

    private final Map<String, Map<DiscordLocale, String>> localizationMap;

    public CommandLocalizationFunction() {
        localizationMap = new HashMap<>();
    }

    public Map<String, Map<DiscordLocale, String>> getLocalizationMap() {
        return localizationMap;
    }

    public void registerLocalization(@NotNull String localizationKey, @NotNull Map<DiscordLocale, String> localization) {
        this.localizationMap.put(localizationKey, localization);
    }

    @NotNull
    @Override
    public Map<DiscordLocale, String> apply(@NotNull String localizationKey) {
        Map<DiscordLocale, String> lookupResult = localizationMap.get(localizationKey);

        return lookupResult != null ? lookupResult : new HashMap<>();
    }
}
