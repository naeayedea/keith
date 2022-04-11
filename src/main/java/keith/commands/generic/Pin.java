package keith.commands.generic;

import keith.commands.IReactionCommand;
import keith.managers.ServerManager;
import keith.managers.ServerManager.Server;
import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Pin extends UserCommand implements IReactionCommand {

    public Pin() {
        super("pin");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+"pin: \"reply with "+prefix+"pin to a message to 'pin' it in a separate channel - useful when channel pins are full!"
                +" See "+prefix+"help pin for more usages!\"";
    }

    @Override
    public String getLongDescription() {
        return "Pin allows users to 'pin' messages to a separate read only channel. They can pin a message by replying, with the message"
                +"id or by using 'pin [text]' to pin the text entered in the message.";
    }

    @Override
    public void run(MessageReactionAddEvent event, User user) {
        MessageChannel channel = event.getChannel();
        Message message = channel.retrieveMessageById(event.getMessageId()).complete();
        String messageContent = message.getContentRaw().trim();
        List<String> tokens = new ArrayList<>(Arrays.asList(messageContent.split("\\s+")));
        Guild guild = event.getGuild();
        Server server = ServerManager.getInstance().getServer(guild.getId());
        MessageChannel pinChannel = getPinChannel(server, guild);
        if (pinChannel == null) {
            //if getPinChannel returns null, then no pin channel exists and bot does not have the permissions to create it
            event.getChannel().sendMessage("No pin channel exists, please give the bot manage channel permissions").queue();
        } else {
            //pin command found, send pin
            sendEmbed(message.getAuthor(), user, message, message, pinChannel, message.getChannel(), guild, MessageType.DEFAULT, tokens);
        }
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
        Server server = ServerManager.getInstance().getServer(guild.getId());
        MessageChannel pinChannel = getPinChannel(server, guild);
        if (pinChannel == null) {
            //if getPinChannel returns null, then no pin channel exists and bot does not have the permissions to create it
            event.getChannel().sendMessage("No pin channel exists, please give the bot manage channel permissions").queue();
        } else {
            //pin command found, send pin
            Message messageSource = getMessageSource(message, tokens);
            if (messageSource == null){
                return;
            }
            sendEmbed(messageSource.getAuthor(), event.getAuthor(), messageSource, message, pinChannel, channel, guild, message.getType(), tokens);
        }
    }

    private MessageChannel getPinChannel(Server server, Guild guild) {
        String pinChannel = server.getPinChannel();
        final TextChannel channel;
        Member selfMember = guild.getMember(Utilities.getJDAInstance().getSelfUser());
        if ((pinChannel.equals("empty") || guild.getTextChannelById(pinChannel) == null ) && selfMember != null) {
            try {
                channel = guild.createTextChannel("pins", null)
                    .addPermissionOverride(selfMember, Permission.ALL_TEXT_PERMISSIONS, 0L)
                    .addPermissionOverride(guild.getPublicRole(), Collections.singleton(Permission.VIEW_CHANNEL), Collections.singleton(Permission.MESSAGE_SEND))
                    .complete();
            server.setPinChannel(channel.getId());
            } catch (InsufficientPermissionException e) {
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
                    return history.getRetrievedHistory().get(0);
                }
            } else {
              //there is some message content to pin therefore return the original message as the source.
              return message;
            }
        }
    }

    private void sendEmbed(User author, User pinner, Message messageSource, Message commandMessage, MessageChannel pinChannel, MessageChannel commandChannel, Guild guild, MessageType type, List<String> tokens){
        List<Message.Attachment> attachments = messageSource.getAttachments();
        EmbedBuilder eb = new EmbedBuilder();
        if (pinChannel.getId().equals(commandChannel.getId())) {
            return;
        }
        String content = messageSource.getContentRaw().trim();
        if (content.equals("") && attachments.isEmpty()) {
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
        eb.setFooter("Message Pinned By " + pinner.getName() +" from "+commandChannel.getName());
        eb.setTimestamp(new Date().toInstant());
        //Do embed stuff
        if(attachments.size() > 0) {
            Message.Attachment attachment = attachments.get(0);
            if (attachment.isImage()) {
                eb.setImage(attachment.getUrl());
            } else {
                eb.appendDescription("[Attached Video]("+attachment.getUrl()+") - download\n\n");
            }
        }
        BaseGuildMessageChannel channel = (BaseGuildMessageChannel) guild.getGuildChannelById(messageSource.getChannel().getId());
        if (channel != null) {
            if (channel.isNSFW()) {
                eb.appendDescription("[Message Link (NSFW)]("+messageSource.getJumpUrl()+")");
            } else {
                eb.appendDescription("[Message Link]("+messageSource.getJumpUrl()+")");
            }
            eb.setTitle("Message From " + author.getName() + "\nSent from "+ channel.getName());
        } else {
            eb.setTitle("Message From " + author.getName());
            eb.appendDescription("[Message Link]("+messageSource.getJumpUrl()+")");
        }
        pinChannel.sendMessageEmbeds(eb.build()).queue((message) -> {
            EmbedBuilder reply = new EmbedBuilder();
            reply.setTitle(":pushpin: Message Pinned!");
            reply.setDescription("[Pinned Message]("+message.getJumpUrl()+")");
            reply.setColor(new Color(155,0,155));
            messageSource.replyEmbeds(reply.build()).queue();
            //commandChannel.sendMessageEmbeds(reply.build()).queue();
        });
    }

}
