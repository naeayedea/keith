package com.naeayedea.keith.platform.discord.command.lib.input.transformer;

import com.naeayedea.keith.common.model.command.input.TextOptionsCommandInput;
import com.naeayedea.keith.core.managers.KeithUserManager;
import com.naeayedea.keith.core.managers.KeithServerManager;
import com.naeayedea.keith.core.managers.cache.KeithMessageChannelCache;
import com.naeayedea.keith.common.model.channel.KeithMessageChannel;
import com.naeayedea.keith.common.model.message.BasicKeithMessage;
import com.naeayedea.keith.common.model.message.KeithMessage;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.utils.KeithDiscordConstants;
import com.naeayedea.keith.platform.discord.command.lib.input.DiscordTextCommandInput;
import com.naeayedea.keith.platform.discord.model.DiscordModelConverter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class DiscordTextInputTransformer {

    private final KeithUserManager keithUserManager;

    private final KeithServerManager serverManager;

    private final DiscordModelConverter modelConverter;
    private final KeithMessageChannelCache keithMessageChannelCache;

    public DiscordTextInputTransformer(KeithUserManager keithUserManager, KeithServerManager serverManager, DiscordModelConverter modelConverter, KeithMessageChannelCache keithMessageChannelCache) {
        this.keithUserManager = keithUserManager;
        this.serverManager = serverManager;
        this.modelConverter = modelConverter;
        this.keithMessageChannelCache = keithMessageChannelCache;
    }

    @NonNull
    public DiscordTextCommandInput fromMessageReceivedEvent(@NonNull MessageReceivedEvent event) {
        return DiscordTextCommandInput.builder()
            .user(keithUserManager.getUser(event.getAuthor().getId()))
            .timestamp(event.getMessage().getTimeCreated().toInstant())
            .message(modelConverter.getKeithMessageFromDiscordMessage(event.getMessage()))
            .build();
    }

    /**
     * Converts a slash command interaction event into a text input by flattening the options into a single string.
     * Only recommended for use when you only have a single option as there's no guarantee of the ordering of inputs.
     * <br/><br/>
     * See {@link TextOptionsCommandInput TextOptionsCommandInput} for a map of options
     * @param event the {@link SlashCommandInteractionEvent} to unpack
     *
     * @return a flattened representation of the command inputs, ignoring the keys
     */
    @NonNull
    public DiscordTextCommandInput fromSlashCommandInteractionEvent(SlashCommandInteractionEvent event) {
        KeithUser user = keithUserManager.getUser(event.getUser().getId());

        KeithMessageChannel channel = keithMessageChannelCache.getChannel(event.getChannel().getId(), KeithDiscordConstants.PLATFORM_NAME);

        Instant timestamp = event.getTimeCreated().toInstant();

        return DiscordTextCommandInput.builder()
            .user(user)
            .timestamp(timestamp)
            .message(flattenSlashCommandOptions(user, channel, event.getOptions(), timestamp))
            .build();
    }

    private KeithMessage flattenSlashCommandOptions(KeithUser user, KeithMessageChannel channel, List<OptionMapping> options, Instant timestamp) {
        List<KeithUser> mentionedUsers = new ArrayList<>();

        List<String> values = new ArrayList<>();

        for (OptionMapping option : options) {
            //add any mentioned users
            mentionedUsers.addAll(option.getMentions().getUsers().stream().map(mentionedUser -> keithUserManager.getUser(mentionedUser.getId())).toList());

            //add the content to the list
            values.add(option.getAsString());
        }

        //finally flatten the content into a string and return an ephemeral message
        return new BasicKeithMessage(user, channel, String.join(" ", values), mentionedUsers, timestamp);

    }
}
