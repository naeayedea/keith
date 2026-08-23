package com.naeayedea.keith.platform.discord.command.general.help;

import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.platform.discord.command.lib.StringSelectInteractionHandler;
import com.naeayedea.keith.platform.discord.server.LocalServerSettingsProvider;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

/**
 * Handles a dropdown selection on a message {@link HelpCommandHandler} sent - updates that same
 * message's embed and dropdown in place, re-selecting whatever was just chosen (including back to
 * the info panel via {@link HelpMenuBuilder#INFO_OPTION_VALUE}).
 *
 * @author naeayedea
 */
@Component
public class HelpSelectHandler implements StringSelectInteractionHandler {

    private final HelpMenuBuilder menuBuilder;

    private final LocalServerSettingsProvider serverSettings;

    @Value("${keith.discord.invite-link:}")
    private String inviteLink;

    @Value("${keith.discord.help.platform-tips:}")
    private String platformTips;

    public HelpSelectHandler(
        HelpMenuBuilder menuBuilder,
        LocalServerSettingsProvider serverSettings
    ) {
        this.menuBuilder = menuBuilder;
        this.serverSettings = serverSettings;
    }

    @Override
    @NonNull
    public List<String> getTriggerOptions() {
        return List.of(HelpMenuBuilder.SELECT_COMPONENT_ID);
    }

    @Override
    public void handleStringSelectEvent(StringSelectInteractionEvent event) throws KeithExecutionException {
        Guild guild = event.getGuild();
        String prefix = guild == null ? "?" : serverSettings.getPrefix(guild.getId());

        String selected = event.getValues().getFirst();
        String userId = event.getUser().getId();

        try {
            TileResponse commandList = menuBuilder.fetchCommandList(userId, prefix);

            TileResponse selectedResponse = HelpMenuBuilder.INFO_OPTION_VALUE.equals(selected)
                ? menuBuilder.fetchInfo(userId, prefix, inviteLink, platformTips)
                : menuBuilder.fetchCommand(userId, prefix, selected);

            StringSelectMenu menu = menuBuilder.buildMenu(commandList, selected);

            event.editMessageEmbeds(menuBuilder.toEmbed(selectedResponse))
                .setComponents(ActionRow.of(menu))
                .queue();
        } catch (HttpClientErrorException e) {
            throw new KeithExecutionException(e);
        }
    }
}
