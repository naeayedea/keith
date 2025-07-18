package com.naeayedea.keith.platform.discord.command.general.ping;

import com.naeayedea.keith.core.commands.drivers.general.ping.PingCommand;
import com.naeayedea.keith.common.model.command.input.TextCommandInput;
import com.naeayedea.keith.common.model.command.output.basic.TextCommandOutput;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.common.i18n.TranslationProvider;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import com.naeayedea.keith.platform.discord.command.lib.input.transformer.DiscordTextInputTransformer;
import com.naeayedea.keith.platform.discord.command.lib.interactions.SlashCommandHandler;
import com.naeayedea.keith.platform.discord.command.lib.output.DiscordTextCommandOutputResponseTransformer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PingCommandHandler extends PingCommand implements TextCommandHandler, SlashCommandHandler {

    private final DiscordPingCommandStrategy commandStrategy;

    private final DiscordTextCommandOutputResponseTransformer responseTransformer;

    private final DiscordTextInputTransformer textInputTransformer;

    public PingCommandHandler(@Value("${keith.commands.ping.internal-name}") String internalName, @Value("${keith.commands.ping.name-key}") String nameKey, @Value("${keith.commands.ping.alias-key}") String aliasKey, TranslationProvider translationProvider, DiscordTextCommandOutputResponseTransformer responseTransformer, DiscordTextInputTransformer textInputTransformer) {
        super(internalName, nameKey, aliasKey);
        this.commandStrategy = new DiscordPingCommandStrategy(translationProvider);
        this.responseTransformer = responseTransformer;
        this.textInputTransformer = textInputTransformer;
    }

    @Override
    public void run(@NonNull MessageReceivedEvent event, @NonNull List<String> tokens) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        TextCommandInput input = textInputTransformer.fromMessageReceivedEvent(event);

        TextCommandOutput commandOutput = run(input, commandStrategy);

        event.getMessage().reply(responseTransformer.transform(commandOutput)).queue();
    }

    @Override
    public void run(@NonNull SlashCommandInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        TextCommandInput input = textInputTransformer.fromSlashCommandInteractionEvent(event);

        TextCommandOutput commandOutput = run(input, commandStrategy);

        event.reply(responseTransformer.transform(commandOutput))
            .setEphemeral(true)
            .queue();
    }
}
