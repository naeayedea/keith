package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.commands.message.ReactionCommand;
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

import static net.dv8tion.jda.api.Permission.*;

@Component
public class Pin extends AbstractUserCommand implements ReactionCommand {

    private final ServerManager serverManager;

    @Value("#{T(com.naeayedea.keith.converter.StringToEmojiConverter).convertList('${keith.commands.pin.reactionTriggers}', ',')}")
    private List<Emoji> reactionTriggers;

    public Pin(ServerManager serverManager, @Value("${keith.commands.pin.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.pin.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.serverManager = serverManager;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + "pin: \"reply with " + prefix + "pin to a message to 'pin' it in a separate channel - useful when channel pins are full!"
            + " See " + prefix + "help pin for more usages!\"";
    }

    @Override
    public String getDescription() {
        return "Pin allows users to 'pin' messages to a separate read only channel. They can pin a message by replying, with the message"
            + "id or by using 'pin [text]' to pin the text entered in the message.";
    }

    @Override
    public List<Emoji> getReactionTriggers() {
        return reactionTriggers;
    }

    @Override
    public boolean triggeredBy(Emoji emoji) {
        return reactionTriggers.contains(emoji);
    }


    @Override
    public void run(MessageReactionAddEvent event, User user) {
        MessageChannel channel = event.getChannel();

        channel.retrieveMessageById(event.getMessageId()).queue(message -> {
            String messageContent = message.getContentRaw().trim();

            List<String> tokens = new ArrayList<>(Arrays.asList(messageContent.split("\\s+")));

            Guild guild = event.getGuild();
            JDA jda = event.getJDA();

            Server server = serverManager.getServer(guild.getId());

            MessageChannel pinChannel = getPinChannel(jda, server, guild);

            if (pinChannel == null) {
                //if getPinChannel returns null, then no pin channel exists and bot does not have the permissions to create it
                event.getChannel().sendMessage("No pin channel exists, please give the bot manage channel permissions").queue();
            } else {
                //pin command found, send pin
                sendEmbed(message.getAuthor(), user, message, message, pinChannel, message.getChannel(), guild, MessageType.DEFAULT, tokens);
            }
        });
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        //ensure message is not in a private channel
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        JDA jda = event.getJDA();

        Server server = serverManager.getServer(guild.getId());

        MessageChannel pinChannel = getPinChannel(jda, server, guild);
        if (pinChannel == null) {
            //if getPinChannel returns null, then no pin channel exists and bot does not have the permissions to create it
            event.getChannel().sendMessage("No pin channel exists, please give the bot manage channel permissions").queue();
        } else {
            //pin command found, send pin
            Message messageSource = getMessageSource(message, tokens);
            if (messageSource == null) {
                return;
            }
            sendEmbed(messageSource.getAuthor(), event.getAuthor(), messageSource, message, pinChannel, channel, guild, message.getType(), tokens);
        }
    }

    private MessageChannel getPinChannel(JDA jda, Server server, Guild guild) {
        String pinChannel = server.pinChannel();

        final TextChannel channel;

        Member selfMember = guild.getMember(jda.getSelfUser());

        if ((pinChannel.equals("empty") || guild.getTextChannelById(pinChannel) == null) && selfMember != null) {
            try {
                channel = guild.createTextChannel("pins", null)
                    .addPermissionOverride(selfMember, getPinChannelPermissions(), 0L)
                    .addPermissionOverride(guild.getPublicRole(), Collections.singleton(Permission.VIEW_CHANNEL), Collections.singleton(MESSAGE_SEND))
                    .complete();

                serverManager.setPinChannel(server.serverID(), channel.getId());
            } catch (InsufficientPermissionException | SQLException e) {
                return null;
            }
        } else {
            channel = guild.getTextChannelById(pinChannel);
        }
        return channel;
    }

    private Message getMessageSource(Message message, List<String> tokens) {
        MessageType type = message.getType();
        MessageChannel channel = message.getChannel();
        if (type.equals(MessageType.INLINE_REPLY)) {
            return message.getReferencedMessage();
        } else {
            //need to garner source from message content
            if (tokens.isEmpty() && message.getAttachments().isEmpty()) {
                //new functionality:
                //fetch last message in channel:
                MessageHistory history = MessageHistory.getHistoryBefore(channel, message.getId()).limit(1).complete();
                if (history.getRetrievedHistory().isEmpty()) {
                    channel.sendMessage("Please input text/images to pin or pin a message by replying with pin or using pin [message id]").queue();
                    return null;
                } else {
                    return history.getRetrievedHistory().getFirst();
                }
            } else {
                //there is some message content to pin therefore return the original message as the source.
                return message;
            }
        }
    }

    private void sendEmbed(User author, User pinner, Message messageSource, Message commandMessage, MessageChannel pinChannel, MessageChannel commandChannel, Guild guild, MessageType type, List<String> tokens) {
        List<Message.Attachment> attachments = messageSource.getAttachments();
        EmbedBuilder eb = new EmbedBuilder();
        if (pinChannel.getId().equals(commandChannel.getId())) {
            return;
        }
        String content = messageSource.getContentRaw().trim();
        if (content.isEmpty() && attachments.isEmpty()) {
            Utilities.Messages.sendError(commandChannel, "No Content", "Message to pin can't be empty");
            return;
        }
        if (type == MessageType.INLINE_REPLY)
            eb.setDescription(content + "\n");
        else {
            if (messageSource.equals(commandMessage)) {
                eb.setDescription(Utilities.stringListToString(tokens) + "\n");
            } else {
                eb.setDescription(content + "\n");
            }
        }
        eb.setColor(Utilities.getMemberColor(guild, author));
        eb.setThumbnail(author.getAvatarUrl());
        eb.setFooter("Message Pinned By " + pinner.getName() + " from " + commandChannel.getName());
        eb.setTimestamp(new Date().toInstant());
        //Do embed stuff
        if (!attachments.isEmpty()) {
            Message.Attachment attachment = attachments.getFirst();
            if (attachment.isImage()) {
                eb.setImage(attachment.getUrl());
            } else {
                eb.appendDescription("[Attached Video](" + attachment.getUrl() + ") - download\n\n");
            }
        }
        TextChannel channel = guild.getTextChannelById(messageSource.getChannel().getId());
        if (channel != null) {
            if (channel.isNSFW()) {
                eb.appendDescription("[Message Link (NSFW)](" + messageSource.getJumpUrl() + ")");
            } else {
                eb.appendDescription("[Message Link](" + messageSource.getJumpUrl() + ")");
            }
            eb.setTitle("Message From " + author.getName() + "\nSent from " + channel.getName());
        } else {
            eb.setTitle("Message From " + author.getName());
            eb.appendDescription("[Message Link](" + messageSource.getJumpUrl() + ")");
        }
        pinChannel.sendMessageEmbeds(eb.build()).queue((message) -> {
            EmbedBuilder reply = new EmbedBuilder();
            reply.setTitle(":pushpin: Message Pinned!");
            reply.setDescription("[Pinned Message](" + message.getJumpUrl() + ")");
            reply.setColor(new Color(155, 0, 155));
            messageSource.replyEmbeds(reply.build()).queue();
            //commandChannel.sendMessageEmbeds(reply.build()).queue();
        });
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
