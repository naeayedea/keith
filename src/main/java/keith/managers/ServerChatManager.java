package keith.managers;

import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerChatManager {

    private static class ChatAgent {
        public MessageChannel channel;
        public boolean isFeedback;
        public ChatAgent(MessageChannel linkedChannel, boolean isFeedback) {
            this.channel = linkedChannel;
            this.isFeedback = isFeedback;
        }
    }

    private static class ChatCandidate {
        public Guild guild;
        public MessageChannel channel;
        public long connectionTime;
        public ChatCandidate (MessageChannel channel, Guild guild, long connectionTime) {
            this.channel = channel;
            this.guild= guild;
            this.connectionTime = connectionTime;
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

        private ChatAgent getTarget(String id) {
            if (agentOne.channel.getId().equals(id)) {
                return agentTwo;
            } else {
                return agentOne;
            }
        }

        private ChatAgent getSelf(String id) {
            if (agentOne.channel.getId().equals(id)) {
                return agentOne;
            } else {
                return agentTwo;
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
    private final Map<String, ChatCandidate> matchmaking;
    private final Lock matchmakingLock;
    public ServerChatManager() {
        chats = new HashMap<>();
        identifiers = new HashMap<>();
        matchmaking = new HashMap<>();
        matchmakingLock = new ReentrantLock();
        feedback = Utilities.getJDAInstance().getTextChannelById("760226764529336350");
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
            ServerManager sm = ServerManager.getInstance();
            Guild linkedGuild = connection.guild;
            MessageChannel linkedChannel = connection.channel;
            createChat(channel, linkedChannel, false);
            //inform both channels of the new connection and advise of ability to not  send
            channel.sendMessage(getConnectionMessage(linkedGuild.getName(), linkedChannel.getName(), sm.getServer(guild.getId()).getPrefix())).queue();
            linkedChannel.sendMessage(getConnectionMessage(guild.getName(), channel.getName(), sm.getServer(linkedGuild.getId()).getPrefix())).queue();
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
        return "Connection made with guild "+guildName+" in channel "+channelName+". Say Hi!" +
                "\n\nYou can use "+prefix+" at the start of your message and it wont be sent!";
    }

    public void createChat(MessageChannel channelOne, MessageChannel channelTwo, boolean isFeedback) {
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
        return chat != null;
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
