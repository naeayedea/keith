package keith.managers;

import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class ServerChatManager {

    private static class ChatAgent {
        public MessageChannel channel;
        public boolean active;
        public ChatAgent(MessageChannel linkedChannel, boolean active) {
            this.channel = linkedChannel;
            this.active = active;
        }
    }

    private static class Chat {

        ChatAgent agentOne;
        ChatAgent agentTwo;

        public Chat(ChatAgent one, ChatAgent two) {
            agentOne = one;
            agentTwo = two;
        }

        public MessageChannel getDestination(String id) {
            return getTarget(id).channel;
        }

        public boolean isActive(String id) {
            return getTarget(id).active;
        }

        private ChatAgent getTarget(String id) {
            if (agentOne.channel.getId().equals(id)) {
                return agentTwo;
            } else {
                return agentOne;
            }
        }

        public void close() {
            agentOne.channel.sendMessage("Chat connection closed").queue();
            agentTwo.channel.sendMessage("Chat connection closed").queue();
        }
    }

    private static ServerChatManager instance;

    private final Map<Long, Chat> chats;
    private final Map<String, Long> identifiers;
    private final MessageChannel feedback;

    public ServerChatManager() {
        chats = new HashMap<>();
        identifiers = new HashMap<>();
        feedback = Utilities.getJDAInstance().getTextChannelById("760226764529336350");
    }

    public MessageChannel getFeedbackChannel() {
        return feedback;
    }

    public void createChat(MessageChannel channelOne, MessageChannel channelTwo, boolean oneWay) {
        long identifier = channelOne.hashCode() | ((long) channelTwo.hashCode() << 32);
        chats.put(identifier, new Chat(new ChatAgent(channelOne, true), new ChatAgent(channelTwo, !oneWay)));
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

        return chat != null && chat.isActive(id);
    }

    public void sendMessage(String id, MessageReceivedEvent event, boolean isFeedback) {
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Utilities.getMemberColor(guild, author));
        eb.setThumbnail(author.getAvatarUrl());
        eb.setDescription(event.getMessage().getContentRaw());
        if (isFeedback) {
            eb.setTitle("Feedback Sent By " + author.getName() +" from "+guild.getName());
        } else {
            eb.setTitle("Message Sent By " + author.getName() +" from "+guild.getName());
        }
        getDestination(id).sendMessageEmbeds(eb.build()).queue();
    }

    public MessageChannel getDestination(String id) {
        return chats.get(identifiers.get(id)).getDestination(id);
    }

    public void closeAll() {
        for (Chat chat : chats.values()) {
            chat.close();
        }
    }

    public static ServerChatManager getInstance() {
        if (instance == null) {
            instance = new ServerChatManager();
        }
        return instance;
    }

}
