package com.naeayedea.keith.platform.discord.command.general.help;

import com.naeayedea.keith.common.model.api.v1.response.tile.FieldTile;
import com.naeayedea.keith.common.model.api.v1.response.tile.Tile;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileContainer;
import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.platform.discord.client.CoreApiClient;
import com.naeayedea.keith.platform.discord.model.DiscordTileConverter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the shared pieces of the interactive help UI - the command/info embeds and the dropdown
 * that switches between them - for both {@link HelpCommandHandler} (the initial {@code ?help}
 * reply) and {@link HelpSelectHandler} (updating that same message in place when the dropdown
 * changes). Both call core the same way ({@link CoreApiClient}) and render with the same
 * {@link DiscordTileConverter} - this exists purely to avoid duplicating that between the two.
 *
 * @author naeayedea
 */
@Component
public class HelpMenuBuilder {

    /**
     * The component id {@link HelpSelectHandler} listens for.
     */
    public static final String SELECT_COMPONENT_ID = "help-command-select";

    /**
     * The dropdown's always-present entry that returns to the info panel - matches
     * {@code HelpService.INFO_OPTION_VALUE} on the core side.
     */
    public static final String INFO_OPTION_VALUE = "info";

    private static final String HELP_PATH = "/api/v1/command/general/help";

    private static final String HELP_COMMANDS_PATH = "/api/v1/command/general/help/commands";

    private final CoreApiClient coreApiClient;

    private final DiscordTileConverter tileConverter;

    public HelpMenuBuilder(
        CoreApiClient coreApiClient,
        DiscordTileConverter tileConverter
    ) {
        this.coreApiClient = coreApiClient;
        this.tileConverter = tileConverter;
    }

    /**
     * @return every registered command, one field each - used only to populate the dropdown's
     * options, never rendered as a page of its own
     */
    @NonNull
    public TileResponse fetchCommandList(@NonNull String platformUserId, @NonNull String prefix) {
        return coreApiClient.getAsUser(HELP_COMMANDS_PATH, platformUserId, Map.of("prefix", prefix), TileResponse.class);
    }

    /**
     * @return the "about this bot" info panel
     */
    @NonNull
    public TileResponse fetchInfo(
        @NonNull String platformUserId,
        @NonNull String prefix,
        @Nullable String inviteLink,
        @Nullable String platformTips
    ) {
        Map<String, String> params = new LinkedHashMap<>();

        params.put("prefix", prefix);

        if (inviteLink != null && !inviteLink.isBlank()) {
            params.put("inviteLink", inviteLink);
        }

        if (platformTips != null && !platformTips.isBlank()) {
            params.put("platformTips", platformTips);
        }

        return coreApiClient.getAsUser(HELP_PATH, platformUserId, params, TileResponse.class);
    }

    /**
     * @throws org.springframework.web.client.HttpClientErrorException.NotFound if commandName doesn't match a
     *                                                                          registered command
     */
    @NonNull
    public TileResponse fetchCommand(
        @NonNull String platformUserId,
        @NonNull String prefix,
        @NonNull String commandName
    ) {
        return coreApiClient.getAsUser(HELP_PATH, platformUserId, Map.of("prefix", prefix, "commandName", commandName), TileResponse.class);
    }

    @NonNull
    public MessageEmbed toEmbed(@NonNull TileResponse response) {
        return tileConverter.toEmbed(response.getTile());
    }

    /**
     * @param commandListResponse the full listing, as returned by {@link #fetchCommandList}
     * @param selected            the command to pre-select, {@link #INFO_OPTION_VALUE}, or {@code null} for
     *                            no pre-selection (bare {@code ?help} always resolves to the info panel, so
     *                            pass {@link #INFO_OPTION_VALUE} there rather than {@code null})
     * @return a dropdown with an "Info" option plus one option per command
     */
    @NonNull
    public StringSelectMenu buildMenu(@NonNull TileResponse commandListResponse, @Nullable String selected) {
        StringSelectMenu.Builder builder = StringSelectMenu.create(SELECT_COMPONENT_ID)
            .setPlaceholder("Choose a command for more information...")
            .addOption("Info", INFO_OPTION_VALUE, "About this bot", null);

        for (FieldTile field : commandFields(commandListResponse)) {
            String description = field.getContent().length() > SelectOption.DESCRIPTION_MAX_LENGTH
                ? field.getContent().substring(0, SelectOption.DESCRIPTION_MAX_LENGTH)
                : field.getContent();

            builder.addOption(field.getName(), field.getName(), description);
        }

        if (selected != null) {
            builder.setDefaultValues(selected);
        }

        return builder.build();
    }

    @NonNull
    private List<FieldTile> commandFields(@NonNull TileResponse response) {
        Tile<?> tile = response.getTile();

        if (!(tile instanceof TileContainer container)) {
            return List.of();
        }

        return container.getTiles().stream()
            .filter(FieldTile.class::isInstance)
            .map(FieldTile.class::cast)
            .toList();
    }
}
