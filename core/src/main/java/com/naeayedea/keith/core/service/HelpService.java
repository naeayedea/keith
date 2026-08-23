package com.naeayedea.keith.core.service;

import com.naeayedea.keith.common.i18n.TranslationProvider;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileContainer;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.core.api.v1.exception.KeithUserFacingException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Builds the responses for the help command: an "about this bot" info panel (the default view and
 * the dropdown's "Info" entry), the full command list (used only to populate that dropdown, not
 * shown as a page of its own), and a single named command's detail - all resolved through
 * {@link CommandRegistry}.
 *
 * <p>The invite link and platform tips shown on the info panel are deliberately not core-side
 * config - core has no concept of what platform is calling it beyond the {@code platform} request
 * param already used for identity. They're supplied by the calling leaf app per request, the same
 * way {@code prefix} already is, so a new leaf (or a new platform environment with a different
 * link) needs no core-side change at all.
 *
 * @author naeayedea
 */
@Component
public class HelpService {

    /**
     * The {@code commandName} value (or its absence) that means "show the info panel", not a real
     * command lookup.
     */
    public static final String INFO_OPTION_VALUE = "info";

    private static final String COMMAND_NOT_FOUND_KEY = "translation.i18n.commands.help.messages.command-not-found";

    private static final String INFO_TITLE_KEY = "translation.i18n.commands.help.info.title";

    private static final String INFO_DESCRIPTION_KEY = "translation.i18n.commands.help.info.description";

    private static final String INFO_INVITE_FIELD_NAME_KEY = "translation.i18n.commands.help.info.invite.name";

    private static final String INFO_INVITE_FIELD_VALUE_KEY = "translation.i18n.commands.help.info.invite.value";

    private static final String INFO_VERSION_FIELD_NAME_KEY = "translation.i18n.commands.help.info.version.name";

    private static final String INFO_USAGE_FIELD_NAME_KEY = "translation.i18n.commands.help.info.usage.name";

    private static final String INFO_USAGE_FIELD_VALUE_KEY = "translation.i18n.commands.help.info.usage.value";

    private static final String INFO_PLATFORM_TIPS_FIELD_NAME_KEY = "translation.i18n.commands.help.info.platform-tips.name";

    /**
     * The registry this service reads command metadata from.
     */
    private final CommandRegistry commandRegistry;

    /**
     * Resolves i18n keys into locale-specific text.
     */
    private final TranslationProvider translationProvider;

    @Value("${keith.project.name}")
    private String projectName;

    @Value("${keith.version}")
    private String version;

    public HelpService(
        @NonNull CommandRegistry commandRegistry,
        @NonNull TranslationProvider translationProvider
    ) {
        this.commandRegistry = commandRegistry;
        this.translationProvider = translationProvider;
    }

    /**
     * @param commandName the specific command to describe, or {@code null}/{@value #INFO_OPTION_VALUE}
     *                     for the info panel
     * @param prefix the calling platform's command prefix, used to format the description
     * @param locale the locale to resolve text in
     * @param inviteLink a leaf-supplied invite/install link, or {@code null} to omit that field
     * @param platformTips leaf-supplied "how to use this here" text, or {@code null} to omit that field
     * @return a {@link TileResponse} for the info panel or a single command's detail
     * @throws KeithUserFacingException with a 404 status if {@code commandName} is a real command
     *                                   name but doesn't match a registered one
     */
    @NonNull
    public TileResponse buildHelpResponse(
        @Nullable String commandName,
        @NonNull String prefix,
        @NonNull Locale locale,
        @Nullable String inviteLink,
        @Nullable String platformTips
    ) throws KeithUserFacingException {
        if (commandName == null || commandName.isBlank() || commandName.equalsIgnoreCase(INFO_OPTION_VALUE)) {
            return buildInfoResponse(locale, inviteLink, platformTips);
        }

        CommandDescriptor command = findCommandOrThrow(commandName, locale);

        String name = translationProvider.getTranslation(command.nameKey(), locale);
        String description = translationProvider.getTranslation(command.descriptionKey(), new Object[]{name, prefix}, locale);

        TileContainer container = TileContainer.builder()
            .title(translationProvider.getTranslation("translation.i18n.commands.help.name", locale))
            .addField(name, description, false)
            .build();

        return TileResponse.of(container);
    }

    /**
     * @return every registered command as one field each - used only to populate the help
     *         dropdown's options, not shown to the user as a page of its own
     */
    @NonNull
    public TileResponse buildCommandListResponse(
        @NonNull String prefix,
        @NonNull Locale locale
    ) {
        TileContainer.Builder builder = TileContainer.builder()
            .title(translationProvider.getTranslation("translation.i18n.commands.help.name", locale));

        for (CommandDescriptor command : commandRegistry.getCommands()) {
            String name = translationProvider.getTranslation(command.nameKey(), locale);
            String description = translationProvider.getTranslation(command.descriptionKey(), new Object[]{name, prefix}, locale);

            builder.addField(name, description, false);
        }

        return TileResponse.of(builder.build());
    }

    @NonNull
    private TileResponse buildInfoResponse(
        @NonNull Locale locale,
        @Nullable String inviteLink,
        @Nullable String platformTips
    ) {
        TileContainer.Builder builder = TileContainer.builder()
            .title(translationProvider.getTranslation(INFO_TITLE_KEY, new Object[]{projectName}, locale))
            .description(translationProvider.getTranslation(INFO_DESCRIPTION_KEY, locale));

        if (inviteLink != null && !inviteLink.isBlank()) {
            builder.addField(
                translationProvider.getTranslation(INFO_INVITE_FIELD_NAME_KEY, locale),
                translationProvider.getTranslation(INFO_INVITE_FIELD_VALUE_KEY, new Object[]{inviteLink}, locale),
                false
            );
        }

        builder.addField(translationProvider.getTranslation(INFO_VERSION_FIELD_NAME_KEY, locale), version, true);

        builder.addField(
            translationProvider.getTranslation(INFO_USAGE_FIELD_NAME_KEY, locale),
            translationProvider.getTranslation(INFO_USAGE_FIELD_VALUE_KEY, locale),
            false
        );

        if (platformTips != null && !platformTips.isBlank()) {
            builder.addField(translationProvider.getTranslation(INFO_PLATFORM_TIPS_FIELD_NAME_KEY, locale), platformTips, false);
        }

        return TileResponse.of(builder.build());
    }

    @NonNull
    private CommandDescriptor findCommandOrThrow(
        @NonNull String commandName,
        @NonNull Locale locale
    ) throws KeithUserFacingException {
        return commandRegistry.findByInternalName(commandName)
            .orElseThrow(() -> new KeithUserFacingException(
                HttpStatus.NOT_FOUND,
                translationProvider.getTranslation(COMMAND_NOT_FOUND_KEY, new Object[]{commandName}, locale)
            ));
    }
}
