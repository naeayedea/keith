package com.naeayedea.keith.managers;

import com.naeayedea.keith.model.chat.Chat;
import com.naeayedea.keith.model.chat.ChatAgent;
import com.naeayedea.keith.model.chat.ChatCandidate;
import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.entities.emoji.UnicodeEmojiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ServerChatManager {

    private final Map<Long, Chat> chats;

    private final Map<String, Long> identifiers;

    private final MessageChannel feedback;

    private final Map<String, ChatCandidate> matchmaking;

    private final Lock matchmakingLock;

    private final Logger logger = LoggerFactory.getLogger(ServerChatManager.class);

    private final ServerManager serverManager;

    public ServerChatManager(JDA jda, ServerManager serverManager) {
        this.serverManager = serverManager;
        this.chats = new HashMap<>();
        this.identifiers = new HashMap<>();
        this.matchmaking = new HashMap<>();
        this.matchmakingLock = new ReentrantLock();

        this.feedback = jda.getTextChannelById("760226764529336350");
    }

    public MessageChannel getFeedbackChannel() {
        return feedback;
    }

    public boolean startMatchmaking(MessageChannel channel, Guild guild) {
        //secure matchmaking lock
        matchmakingLock.lock();
        if (matchmaking.isEmpty()) {
            //nobody in matchmaking queue, add to queue and remove lock
            matchmaking.put(channel.getId(), new ChatCandidate(channel, guild, System.currentTimeMillis()));
            matchmakingLock.unlock();
            return false;
        } else {
            //get first result
            ChatCandidate connection = matchmaking.remove(matchmaking.keySet().iterator().next());
            matchmakingLock.unlock();
            Guild linkedGuild = connection.guild();
            MessageChannel linkedChannel = connection.channel();
            createChat(channel, linkedChannel, false);
            //inform both channels of the new connection and advise of ability to not  send
            channel.sendMessage(getConnectionMessage(linkedGuild.getName(), linkedChannel.getName(), serverManager.getServer(guild.getId()).prefix())).queue();
            linkedChannel.sendMessage(getConnectionMessage(guild.getName(), channel.getName(), serverManager.getServer(linkedGuild.getId()).prefix())).queue();
            return true;
        }
    }

    public boolean stopMatchmaking(String id) {
        matchmakingLock.lock();
        ChatCandidate result = matchmaking.remove(id);
        matchmakingLock.unlock();
        return result != null;
    }

    private String getConnectionMessage(String guildName, String channelName, String prefix) {
        return "Connection made with guild " + guildName + " in channel " + channelName + ". Say Hi!" +
            "\n\nYou can use " + prefix + " at the start of your message and it wont be sent!";
    }

    public void createChat(MessageChannel channelOne, MessageChannel channelTwo, boolean isFeedback) {
        long identifier = channelOne.hashCode() | ((long) channelTwo.hashCode() << 32);
        chats.put(identifier, new Chat(new ChatAgent(channelOne, isFeedback), new ChatAgent(channelTwo, false)));
        identifiers.put(channelOne.getId(), identifier);
        identifiers.put(channelTwo.getId(), identifier);
    }

    //Deletes the chat associated
    public void closeChat(String id) {
        Chat chat = chats.remove(identifiers.get(id));
        if (chat != null) {
            chat.close();
        }
    }

    public boolean hasActiveChat(String id) {
        Chat chat = chats.get(identifiers.get(id));
        return chat != null;
    }

    public void sendMessage(String id, MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        EmbedBuilder eb = new EmbedBuilder();
        User author = event.getAuthor();
        String name;
        String content = message.getContentRaw();
        //Check for message from private channel and adjust name accordingly
        if (channel instanceof PrivateChannel) {
            name = "Private Message";
            eb.setColor(Utilities.getBotColor());
        } else {
            Guild guild = event.getGuild();
            name = event.getGuild().getName();
            eb.setColor(Utilities.getMemberColor(guild, author));
        }
        //Check if the user has sent any embeds
        if (!message.getAttachments().isEmpty()) {
            Message.Attachment attachment = message.getAttachments().getFirst();
            eb.setImage(attachment.getUrl());
        } else if (!message.getEmbeds().isEmpty()) {
            MessageEmbed embed = message.getEmbeds().getFirst();
            String link = embed.getUrl();
            if (link != null) {
                if (embed.getType().equals(EmbedType.IMAGE)) {
                    eb.setImage(link);
                    content = content.replace(link, "");
                } else {
                    try {
                        URL firstLink = (new URI(link)).toURL();
                        HttpURLConnection getVideo = (HttpURLConnection) firstLink.openConnection();
                        getVideo.setRequestMethod("GET");
                        getVideo.connect();
                        String videoURL = Utilities.getVideoURL(Utilities.readInputStream(getVideo.getInputStream()));
                        content = content.replace(link, "");
                        eb.setImage(videoURL);
                    } catch (IOException | URISyntaxException ignored) {}
                }
            }
        }
        eb.setDescription(content);
        eb.setThumbnail(author.getAvatarUrl());
        if (isFeedback(id)) {
            eb.setTitle("Feedback sent by " + author.getName() + " from " + name);
        } else {
            eb.setTitle("Message sent by " + author.getName() + " from " + name);
        }
        getDestination(id).sendMessageEmbeds(eb.build()).queue(result -> message.addReaction(new UnicodeEmojiImpl("U+2709")).queue());
    }

    public MessageChannel getDestination(String id) {
        return chats.get(identifiers.get(id)).getDestination(id);
    }

    public boolean isFeedback(String id) {
        return chats.get(identifiers.get(id)).getSelf(id).isFeedback();
    }

    @PreDestroy
    public void closeAll() {
        logger.info("Classing all chat instances");
        for (Chat chat : chats.values()) {
            chat.close();
        }
    }

}
