package succ.commands.generic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.ServerManager;
import java.awt.*;
import java.io.File;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Allows users to "pin" messages by sending messages to a custom read only pins channel
 * useful when the pins are full or a central pool of pins is wanted rather than channel
 * specific
 */
public class Pin extends UserCommand {

    ServerManager serverManager;
    enum Type{IMAGE, REGULAR};

    public Pin(ServerManager serverManager){
            this.serverManager = serverManager;
    }

    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "pin: \"'pins' a message to a read only channel, useful if your pins are full!, do !pin [messageid] or reply to a message with !ping\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        Message message = event.getMessage();
        User user = event.getAuthor();
        Guild guild = event.getGuild();
        String prefix = getPrefix(event, serverManager);
        if (message.getType().equals(MessageType.INLINE_REPLY)) {
            Message originalMessage = message.getReferencedMessage();
            User author = originalMessage.getAuthor();
            if (originalMessage == null) {
                //Message either unavailable or deleted
                event.getChannel().sendMessage("Couldn't find message").queue();
            } else {
                MessageChannel channel = guild.getTextChannelById(serverManager.getPinChannelID(guild.getId()));
                if(channel != null) {
                    sendEmbed(author, user, originalMessage, channel, event.getChannel(), guild, Type.REGULAR);
                } else {
                    event.getChannel().sendMessage("Could not find pin channel - please add with "+prefix+"pin add [channelid]").queue();
                }
            }
        } else if (message.getAttachments().size() > 0) {
            //User is trying to pin an image
            MessageChannel channel = guild.getTextChannelById(serverManager.getPinChannelID(guild.getId()));
            if(channel != null) {
                sendEmbed(user, user, message, channel, event.getChannel(), guild, Type.IMAGE);
            } else {
                event.getChannel().sendMessage("Could not find pin channel - please add with "+prefix+"pin add [channelid]").queue();
            }
        } else  {
            String command = event.getMessage().getContentRaw();
            String[] commandSplit = command.split("\\s+");
            if(commandSplit.length == 2) {
                try {
                    long messageId = Long.parseLong(commandSplit[1]);
                    Message originalMessage[] = new Message[1];
                    event.getChannel().retrieveMessageById(messageId).queue(msg -> {originalMessage[0] = msg;
                        System.out.println(originalMessage[0].getContentRaw());}, failure -> {
                        System.out.println(":(");
                    });
                    Thread.sleep(500);
                    if(originalMessage[0] == null) {
                        event.getChannel().sendMessage("Could not locate message - try reply with "+prefix+"pin instead!").queue();
                    } else {
                        MessageChannel channel = guild.getTextChannelById(serverManager.getPinChannelID(guild.getId()));
                        if(channel != null) {
                            sendEmbed(originalMessage[0].getAuthor(), user, originalMessage[0], channel, event.getChannel(), guild, Type.REGULAR);
                        } else {
                            event.getChannel().sendMessage("Could not find pin channel - please add with "+prefix+"pin add [channelid]").queue();
                        }
                    }
                } catch (NumberFormatException e){
                    event.getChannel().sendMessage("Invalid message id, please use "+ prefix + "pin [message id]").queue();
                } catch (InterruptedException e){
                    return;
                }
            } else {
                event.getChannel().sendMessage("Invalid input for pin, please use "+ prefix + "pin [message id] "+
                        "or reply to a message you want to pin and type " + prefix + "pin").queue();
            }
        }
    }

    private void sendEmbed(User author, User pinner, Message originalMessage, MessageChannel pinChannel, MessageChannel commandChannel, Guild guild, Type type){
        List<Message.Attachment> attachments = originalMessage.getAttachments();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Message From " + author.getName());
        if(type == Type.REGULAR)
            eb.setDescription(originalMessage.getContentDisplay() + "\n");
        else
            eb.setDescription((originalMessage.getContentDisplay()).substring(4) + "\n");
        eb.setColor(getColour(guild, author));
        eb.setThumbnail(author.getAvatarUrl());
        eb.setFooter("Message Pinned By " + pinner.getName());
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
        pinChannel.sendMessage(eb.build()).queue((message) -> {
            EmbedBuilder reply = new EmbedBuilder();
            reply.setTitle(":pushpin: Message Pinned!");
            reply.setDescription("[Pinned Message]("+message.getJumpUrl()+")");
            reply.setColor(new Color(155,0,155));
            commandChannel.sendMessage(reply.build()).queue();
        });
    }



    private Color getColour(Guild guild, User user){
        Member member = guild.getMemberById(user.getId());
        List<Role> roles = member.getRoles();
        for (Role role : roles) {
            Color color = role.getColor();
            if(color != null) {
                return color;
            }
        }
        return new Color(44,47,51);
    }
}
