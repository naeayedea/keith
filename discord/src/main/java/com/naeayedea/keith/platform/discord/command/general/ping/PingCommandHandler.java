package com.naeayedea.keith.platform.discord.command.general.ping;

import com.naeayedea.keith.common.model.api.v1.response.basic.TextResponse;
import com.naeayedea.keith.common.model.command.CommandInformationProvider;
import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.client.CoreApiClient;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import com.naeayedea.keith.platform.discord.command.lib.interactions.SlashCommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ping, rebuilt as an HTTP client of core rather than extending core's {@code PingCommand} and
 * running its logic in-process - see {@link CoreApiClient}. This is the reference example for
 * every other command handler once they're migrated the same way in a later stage.
 *
 * @author naeayedea
 */
@Component
public class PingCommandHandler implements TextCommandHandler, SlashCommandHandler, CommandInformationProvider {

    private static final String PING_PATH = "/api/v1/command/general/ping";

    private final String internalName;

    private final String nameKey;

    private final String aliasKey;

    private final CoreApiClient coreApiClient;

    public PingCommandHandler(
        @Value("${keith.commands.ping.internal-name}") String internalName,
        @Value("${keith.commands.ping.name-key}") String nameKey,
        @Value("${keith.commands.ping.alias-key}") String aliasKey,
        CoreApiClient coreApiClient
    ) {
        this.internalName = internalName;
        this.nameKey = nameKey;
        this.aliasKey = aliasKey;
        this.coreApiClient = coreApiClient;
    }

    @Override
    public void run(
        @NonNull MessageReceivedEvent event,
        @NonNull List<String> tokens
    ) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        TextResponse response = coreApiClient.getAsUser(PING_PATH, event.getAuthor().getId(), TextResponse.class);

        event.getMessage().reply(response.getText()).queue();
    }

    @Override
    public void run(
        @NonNull SlashCommandInteractionEvent event
    ) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        TextResponse response = coreApiClient.getAsUser(PING_PATH, event.getUser().getId(), TextResponse.class);

        event.reply(response.getText())
            .setEphemeral(true)
            .queue();
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
