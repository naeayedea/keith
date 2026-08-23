package com.naeayedea.keith.platform.discord.command.general.guess;

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Starts a number-guessing game - the actual scoring happens through the generic channel-evaluate
 * path once {@link ActiveSessionRegistry} knows this channel is playing, not here.
 *
 * @author naeayedea
 */
@Component
public class GuessCommandHandler implements TextCommandHandler, CommandInformationProvider {

    private static final String GUESS_PATH = "/api/v1/command/general/guess";

    private final String internalName;

    private final String nameKey;

    private final String aliasKey;

    private final CoreApiClient coreApiClient;

    private final ActiveSessionRegistry activeSessionRegistry;

    public GuessCommandHandler(
        @Value("${keith.commands.guess.internal-name}") String internalName,
        @Value("${keith.commands.guess.name-key}") String nameKey,
        @Value("${keith.commands.guess.alias-key}") String aliasKey,
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
    public void run(
        @NonNull MessageReceivedEvent event,
        @NonNull List<String> tokens
    ) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        Map<String, String> params = new LinkedHashMap<>();

        params.put("channelId", event.getChannel().getId());

        if (tokens.size() > 1) {
            try {
                params.put("number", String.valueOf(Integer.parseInt(tokens.get(1))));
            } catch (NumberFormatException e) {
                event.getMessage().reply("Please enter a whole number, e.g. `guess 500`.").queue();

                return;
            }
        }

        TextResponse response = coreApiClient.getAsUser(GUESS_PATH, event.getAuthor().getId(), params, TextResponse.class);

        //optimistic local update - the SessionStateEvent broadcast will also mark it, but this
        //replica already knows the outcome without waiting on the round trip through the broker
        activeSessionRegistry.markActive(event.getChannel().getId());

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
