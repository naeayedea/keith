package com.naeayedea.keith.platform.discord.command.general.help;

import com.naeayedea.keith.common.model.api.v1.response.tile.TileResponse;
import com.naeayedea.keith.common.model.command.CommandInformationProvider;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import com.naeayedea.keith.platform.discord.server.LocalServerSettingsProvider;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

/**
 * Help, rendered as an embed plus a dropdown ({@link HelpMenuBuilder}) that switches between the
 * info panel and each command's detail in place. {@code ?help} shows the info panel with "Info"
 * pre-selected; {@code ?help <command>} shows that command's detail with it pre-selected instead.
 * Selecting a different option is handled by {@link HelpSelectHandler}, not here.
 *
 * @author naeayedea
 */
@Component
public class HelpCommandHandler implements TextCommandHandler, CommandInformationProvider {

    private static final Logger logger = LoggerFactory.getLogger(HelpCommandHandler.class);

    private final String internalName;

    private final String nameKey;

    private final String aliasKey;

    private final HelpMenuBuilder menuBuilder;

    private final LocalServerSettingsProvider serverSettings;

    @Value("${keith.discord.invite-link:}")
    private String inviteLink;

    @Value("${keith.discord.help.platform-tips:}")
    private String platformTips;

    public HelpCommandHandler(
        @Value("${keith.commands.help.internal-name}") String internalName,
        @Value("${keith.commands.help.name-key}") String nameKey,
        @Value("${keith.commands.help.alias-key}") String aliasKey,
        HelpMenuBuilder menuBuilder,
        LocalServerSettingsProvider serverSettings
    ) {
        this.internalName = internalName;
        this.nameKey = nameKey;
        this.aliasKey = aliasKey;
        this.menuBuilder = menuBuilder;
        this.serverSettings = serverSettings;
    }

    @Override
    public void run(
        @NonNull MessageReceivedEvent event,
        @NonNull List<String> tokens
    ) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        String prefix = event.getChannel() instanceof PrivateChannel
            ? "?"
            : serverSettings.getPrefix(event.getGuild().getId());

        //tokens[0] is the alias itself ("help"); tokens[1], if present, is the target command
        @Nullable String targetCommand = tokens.size() > 1 ? tokens.get(1) : null;

        String userId = event.getAuthor().getId();

        try {
            TileResponse commandList = menuBuilder.fetchCommandList(userId, prefix);

            TileResponse embedResponse = targetCommand == null
                ? menuBuilder.fetchInfo(userId, prefix, inviteLink, platformTips)
                : menuBuilder.fetchCommand(userId, prefix, targetCommand);

            String selected = targetCommand == null ? HelpMenuBuilder.INFO_OPTION_VALUE : targetCommand;

            StringSelectMenu menu = menuBuilder.buildMenu(commandList, selected);

            event.getMessage().replyEmbeds(menuBuilder.toEmbed(embedResponse))
                .addComponents(ActionRow.of(menu))
                .queue();
        } catch (HttpClientErrorException.NotFound e) {
            event.getMessage().reply("No command found with that name.").queue();
        } catch (HttpClientErrorException e) {
            logger.error("Help command failed calling core", e);

            throw new KeithExecutionException(e);
        }
    }

    @Override
    @NonNull
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

    @Override
    public int getTimeOut() {
        return 10;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return true;
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    @NonNull
    public String getInternalName() {
        return internalName;
    }

    @Override
    @NonNull
    public String getAliasTranslationKey() {
        return aliasKey;
    }

    @Override
    @NonNull
    public String getNameTranslationKey() {
        return nameKey;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
