package com.naeayedea.keith.platform.discord.command.general.chat;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.command.CommandInformationProvider;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.client.CoreApiClient;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import com.naeayedea.keith.platform.discord.event.ActiveSessionRegistry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Starts/cancels/closes an inter-server chat connection - relaying messages once connected
 * happens through the generic channel-evaluate path once {@link ActiveSessionRegistry} knows this
 * channel is connected, not here.
 *
 * @author naeayedea
 */
@Component
public class ChatCommandHandler implements TextCommandHandler, CommandInformationProvider {

    private static final String CHAT_PATH = "/api/v1/command/general/chat";

    private final String internalName;

    private final String nameKey;

    private final String aliasKey;

    private final CoreApiClient coreApiClient;

    private final ActiveSessionRegistry activeSessionRegistry;

    public ChatCommandHandler(
        @Value("${keith.commands.chat.internal-name}") String internalName,
        @Value("${keith.commands.chat.name-key}") String nameKey,
        @Value("${keith.commands.chat.alias-key}") String aliasKey,
        CoreApiClient coreApiClient,
        ActiveSessionRegistry activeSessionRegistry
    ) {
        this.internalName = internalName;
        this.nameKey = nameKey;
        this.aliasKey = aliasKey;
        this.coreApiClient = coreApiClient;
        this.activeSessionRegistry = activeSessionRegistry;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(
        @NonNull MessageReceivedEvent event,
        @NonNull List<String> tokens
    ) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        String action = tokens.size() > 1 ? tokens.get(1) : "";

        if (!action.equals("start") && !action.equals("cancel") && !action.equals("close")) {
            event.getMessage().reply("Use `chat start` to find a chat partner, `chat cancel` to stop searching, or `chat close` to end an active chat.").queue();

            return;
        }

        String channelId = event.getChannel().getId();

        TextResponse response = coreApiClient.getAsUser(
            CHAT_PATH,
            event.getAuthor().getId(),
            Map.of("channelId", channelId, "action", action),
            TextResponse.class
        );

        //optimistic local update for the two cases that always leave this channel in a known
        //state; "start" might just be queued (no session yet), so that one's left to the broker
        if (action.equals("close")) {
            activeSessionRegistry.markInactive(channelId);
        }

        event.getMessage().reply(response.getText()).queue();
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
