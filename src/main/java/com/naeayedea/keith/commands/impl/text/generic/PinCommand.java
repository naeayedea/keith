package com.naeayedea.keith.commands.impl.text.generic;

import com.naeayedea.keith.commands.lib.command.interactions.MessageContextCommand;
import com.naeayedea.keith.commands.lib.command.interactions.SlashCommand;
import com.naeayedea.keith.commands.lib.command.ReactionCommand;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.exception.KeithGracefulErrorException;
import com.naeayedea.keith.exception.KeithPermissionException;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.model.Server;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.*;

import static net.dv8tion.jda.api.Permission.*;

@Component
public class PinCommand extends AbstractUserCommand implements ReactionCommand, SlashCommand, MessageContextCommand {

    private final ServerManager serverManager;

    @Value("#{T(com.naeayedea.keith.converter.StringToEmojiConverter).convertList('${keith.commands.pin.reactionTriggers}', ',')}")
    private List<Emoji> reactionTriggers;

    public PinCommand(ServerManager serverManager, @Value("${keith.commands.pin.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.pin.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.serverManager = serverManager;
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + "pin: \"reply with " + prefix + "pin to a message to 'pin' it in a separate channel - useful when channel pins are full!"
            + " See " + prefix + "help pin for more usages!\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Pin allows users to 'pin' messages to a separate read only channel. They can pin a message by replying, with the message"
            + "id or by using 'pin [text]' to pin the text entered in the message.";
    }

    @NotNull
    @Override
    public List<Emoji> getReactionTriggers() {
        return reactionTriggers;
    }

    @Override
    public boolean triggeredBy(@NotNull Emoji emoji) {
        return reactionTriggers.contains(emoji);
    }


    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(@NotNull MessageReactionAddEvent event, @NotNull User user) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        MessageChannel channel = event.getChannel();

        Message messageSource = channel.retrieveMessageById(event.getMessageId()).complete();

        Guild guild = event.getGuild();
        JDA jda = event.getJDA();

        Server server = serverManager.getServer(guild.getId());

        MessageChannel pinChannel = getPinChannel(jda, server, guild);

        //build the embed
        MessageEmbed pinEmbed = getPinEmbed(event.getGuild(), user, messageSource.getAuthor(), event.getChannel().getName(), messageSource.getJumpUrl(), messageSource.getContentRaw(), messageSource.getAttachments(), Utilities.channelIsNSFW(event.getGuildChannel()));

        //send the embed
        pinChannel.sendMessageEmbeds(pinEmbed)
            .queue(message -> event.getChannel().sendMessageEmbeds(getResponseEmbed(message))
                .queue()
            );
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        MessageChannel pinChannel = getPinChannel(event.getJDA(), serverManager.getServer(event.getGuild().getId()), event.getGuild());

        Message messageSource = getMessageSource(event.getMessage(), tokens);

        //build the embed
        MessageEmbed pinEmbed = getPinEmbed(event.getGuild(), event.getAuthor(), messageSource.getAuthor(), event.getChannel().getName(), messageSource.getJumpUrl(), messageSource.getContentRaw(), messageSource.getAttachments(), Utilities.channelIsNSFW(event.getGuildChannel()));

        //send the embed
        pinChannel.sendMessageEmbeds(pinEmbed)
            .queue(message -> event.getMessage()
                .replyEmbeds(getResponseEmbed(message))
                .queue()
            );
    }

    @Override
    public void run(@NotNull SlashCommandInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        if (event.getGuild() == null) {
            throw new KeithExecutionException("Command was unexpectedly run outside a guild.");
        }

        //get the pin channel
        MessageChannel pinChannel = getPinChannel(event.getJDA(), serverManager.getServer(event.getGuild().getId()), event.getGuild());

        OptionMapping pinContentMapping = event.getOption("message");

        if (pinContentMapping == null) {
            throw new KeithGracefulErrorException("Message to pin cannot be empty.");
        }

        //build the embed
        MessageEmbed pinEmbed = getPinEmbed(event.getGuild(), event.getUser(), event.getUser(), "", "", pinContentMapping.getAsString(), new ArrayList<>(), false);

        //send the embed
        pinChannel.sendMessageEmbeds(pinEmbed)
            .queue(message -> event
                .replyEmbeds(getResponseEmbed(message))
                .setEphemeral(false)
                .queue()
            );
    }

    @Override
    public void run(@NotNull MessageContextInteractionEvent event) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        if (event.getGuild() == null) {
            throw new KeithExecutionException("Command was unexpectedly run outside a guild.");
        }

        //get the pin channel
        MessageChannel pinChannel = getPinChannel(event.getJDA(), serverManager.getServer(event.getGuild().getId()), event.getGuild());

        Message sourceMessage = event.getTarget();

        //build the embed
        MessageEmbed pinEmbed = getPinEmbed(event.getGuild(), event.getUser(), event.getUser(), sourceMessage.getChannel().getName(), sourceMessage.getJumpUrl(), sourceMessage.getContentRaw(), new ArrayList<>(), false);

        //send the embed
        pinChannel.sendMessageEmbeds(pinEmbed)
            .queue(message -> event
                .replyEmbeds(getResponseEmbed(message))
                .setEphemeral(false)
                .queue()
            );
    }

    @NotNull
    private MessageEmbed getPinEmbed(@NotNull Guild guild, @NotNull User author, @NotNull User userRunningCommand, @NotNull String source, @NotNull String jumpURL, @NotNull String content, @NotNull List<Message.Attachment> attachments, boolean isNSFW) throws KeithGracefulErrorException {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        content = content.trim();

        if (content.isEmpty() && attachments.isEmpty()) {
            throw new KeithGracefulErrorException("Message to pin can't be empty");
        }

        embedBuilder.setColor(Utilities.getMemberColor(guild, author));
        embedBuilder.setThumbnail(author.getAvatarUrl());
        embedBuilder.setDescription(content + "\n");
        embedBuilder.setFooter("Message Pinned By " + userRunningCommand.getName() + (source.isEmpty() ? " from " + source : ""));
        embedBuilder.setTimestamp(Instant.now());

        //Do embed stuff
        if (!attachments.isEmpty()) {
            Message.Attachment attachment = attachments.getFirst();
            if (attachment.isImage()) {
                embedBuilder.setImage(attachment.getUrl());
            } else {
                embedBuilder.appendDescription("[Attached Video](" + attachment.getUrl() + ") - download\n\n");
            }
        }

        //if jump url exists then we are pinning an existing message and want to warn if the channel is nsfw
        if (jumpURL.isEmpty()) {
            //this is a plain text message
            embedBuilder.setTitle("Message From " + userRunningCommand.getName());
        } else {
            //this is an existing message
            embedBuilder.setTitle("Message From " + author.getName());

            if (isNSFW) {
                embedBuilder.appendDescription("[Message Link (NSFW)](" + jumpURL + ")");
            } else {
                embedBuilder.appendDescription("[Message Link](" + jumpURL + ")");
            }
        }

        return embedBuilder.build();
    }

    private MessageEmbed getResponseEmbed(Message message) {
        EmbedBuilder reply = new EmbedBuilder();
        reply.setTitle(":pushpin: Message Pinned!");
        reply.setDescription("[Pinned Message](" + message.getJumpUrl() + ")");
        reply.setColor(new Color(155, 0, 155));

        return reply.build();
    }

    @NotNull
    private MessageChannel getPinChannel(@NotNull JDA jda, @NotNull Server server, @NotNull Guild guild) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        String pinChannel = server.pinChannel();

        TextChannel channel;

        Member selfMember = guild.getMember(jda.getSelfUser());

        if ((pinChannel.equals("empty") || guild.getTextChannelById(pinChannel) == null) && selfMember != null) {
            try {
                channel = guild.createTextChannel("pins", null)
                    .addPermissionOverride(selfMember, getPinChannelPermissions(), 0L)
                    .addPermissionOverride(guild.getPublicRole(), Collections.singleton(Permission.VIEW_CHANNEL), Collections.singleton(MESSAGE_SEND))
                    .complete();

                serverManager.setPinChannel(server.serverID(), channel.getId());
            } catch (InsufficientPermissionException e) {
                throw new KeithPermissionException(e.getMessage());
            } catch (SQLException e) {
                throw new KeithExecutionException(e.getMessage());
            }
        } else {
            channel = guild.getTextChannelById(pinChannel);
        }

        if (channel == null) {
            throw new KeithGracefulErrorException("No pin channel exists, please give the bot manage channel permissions");
        }

        return channel;
    }

    @NotNull
    private Message getMessageSource(Message message, List<String> tokens) throws KeithGracefulErrorException{
        MessageType type = message.getType();
        MessageChannel channel = message.getChannel();
        if (type.equals(MessageType.INLINE_REPLY)) {
            Message referencedMessage = message.getReferencedMessage();

            if (referencedMessage == null) {
                throw new KeithGracefulErrorException("Expected reply but found no message.");
            }

            return referencedMessage;
        } else {
            //need to garner source from message content
            if (tokens.isEmpty() && message.getAttachments().isEmpty()) {
                //new functionality:
                //fetch last message in channel:
                MessageHistory history = MessageHistory.getHistoryBefore(channel, message.getId()).limit(1).complete();
                if (history.getRetrievedHistory().isEmpty()) {
                    throw new KeithGracefulErrorException("Please input text/images to pin or pin a message by replying with pin or using pin [message id]");
                } else {
                    return history.getRetrievedHistory().getFirst();
                }
            } else {
                //there is some message content to pin therefore return the original message as the source.
                return message;
            }
        }
    }

    private long getPinChannelPermissions() {
        return Permission.getRaw(MESSAGE_ADD_REACTION,
            MESSAGE_SEND,
            MESSAGE_TTS,
            MESSAGE_MANAGE,
            MESSAGE_EMBED_LINKS,
            MESSAGE_ATTACH_FILES,
            MESSAGE_EXT_EMOJI,
            MESSAGE_EXT_STICKER,
            MESSAGE_HISTORY,
            MESSAGE_MENTION_EVERYONE,
            USE_APPLICATION_COMMANDS,
            USE_EXTERNAL_APPLICATIONS,
            USE_EMBEDDED_ACTIVITIES,
            MANAGE_THREADS,
            CREATE_PUBLIC_THREADS,
            CREATE_PRIVATE_THREADS,
            MESSAGE_SEND_IN_THREADS,
            MESSAGE_ATTACH_VOICE_MESSAGE,
            MESSAGE_SEND_POLLS
        );
    }

}
