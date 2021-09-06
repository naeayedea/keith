package keith.commands.generic;

import keith.managers.ServerManager;
import keith.managers.ServerManager.Server;
import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.Date;
import java.util.List;

public class Pin extends UserCommand{

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
    public String getDefaultName() {
        return "pin";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        //ensure message is not in a private channel
        MessageChannel channel = event.getChannel();
        if (channel instanceof PrivateChannel) {
            channel.sendMessage("This command cannot be used in a private channel!").queue();
            return;
        }

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
            System.out.println(messageSource);
            if (messageSource == null){
                return;
            }
            sendEmbed(messageSource.getAuthor(), event.getAuthor(), messageSource, pinChannel, channel, guild, message.getType(), tokens);
        }
    }

    private MessageChannel getPinChannel(Server server, Guild guild) {
        String pinChannel = server.getPinChannel();
        final TextChannel channel;
        if (pinChannel.equals("empty")) {
            try {
            channel = guild.createTextChannel("pins", null).complete();
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

            //check for pinning message
            if (tokens.size() == 1) {
                try {
                    long potentialMessageId = Long.parseLong(tokens.get(0));
                    return channel.retrieveMessageById(potentialMessageId).complete();
                } catch (NumberFormatException e) {
                    return message;
                }
            } else if (tokens.isEmpty() && message.getAttachments().isEmpty()) {
                //invalid input, no reply and no message content
                channel.sendMessage("Please input text/images to pin or pin a message by replying with pin or using pin [message id]").queue();
                return null;
            } else {
              //there is some message content to pin therefore return the original message as the source.
              return message;
            }
        }
    }

    private void sendEmbed(User author, User pinner, Message originalMessage, MessageChannel pinChannel, MessageChannel commandChannel, Guild guild, MessageType type, List<String> tokens){
        List<Message.Attachment> attachments = originalMessage.getAttachments();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Message From " + author.getName());
        String content = originalMessage.getContentRaw().trim();
        if(type  == MessageType.INLINE_REPLY)
            eb.setDescription(content + "\n");
        else {
            String description = content.substring(content.indexOf(" ") + 1) + "\n";
            if (!tokens.isEmpty()) {
                try {
                    Long.parseLong(tokens.get(0));
                    eb.setDescription(content + "\n");
                } catch (NumberFormatException e) {
                    eb.setDescription(description);
                }
            } else {
                eb.setDescription(description);
            }
        }
        eb.setColor(Utilities.getMemberColor(guild, author));
        eb.setThumbnail(author.getAvatarUrl());
        eb.setFooter("Message Pinned By " + pinner.getName() +" from "+commandChannel.getName());
        eb.setTimestamp(new Date().toInstant());
        //Do embed stuff
        if(attachments.size() > 0) {
            Message.Attachment attachment = attachments.get(0);
            if(attachment.isImage()) {
                eb.setImage(attachment.getUrl());

            } else {
                eb.appendDescription("[Attached Video]("+attachment.getUrl()+")\n\n");
            }
        }
        eb.appendDescription("[Message Link]("+originalMessage.getJumpUrl()+")");
        pinChannel.sendMessageEmbeds(eb.build()).queue((message) -> {
            EmbedBuilder reply = new EmbedBuilder();
            reply.setTitle(":pushpin: Message Pinned!");
            reply.setDescription("[Pinned Message]("+message.getJumpUrl()+")");
            reply.setColor(new Color(155,0,155));
            commandChannel.sendMessageEmbeds(reply.build()).queue();
        });
    }

}
