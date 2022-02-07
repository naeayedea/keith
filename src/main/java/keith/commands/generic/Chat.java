package keith.commands.generic;

import keith.managers.ServerChatManager;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Chat extends UserCommand {

    private final String defaultName;
    private final ServerChatManager chatManager;

    public Chat() {
        defaultName = "chat";
        chatManager = ServerChatManager.getInstance();
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"\"";
    }

    @Override
    public String getLongDescription() {
        return "";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (channel instanceof PrivateChannel) {
            channel.sendMessage("Feedback not supported in private channels").queue();
            return;
        }
        String id = channel.getId();
        boolean active = chatManager.hasActiveChat(id);
        //check if the user is looking to close the chat session
        if (!tokens.isEmpty() && tokens.get(0).equalsIgnoreCase("start")) {
            if (active) {
                channel.sendMessage("Chat already in progress, please use another channel or end the previous session").queue();
            } else {
                if (!chatManager.startMatchmaking(channel, event.getGuild())) {
                    channel.sendMessage("Matchmaking started (could take a while)").queue();
                }
            }
            return;
        }

        if (active && !tokens.isEmpty() && tokens.get(0).equalsIgnoreCase("close")) {
            chatManager.closeChat(channel.getId());
            return;
        }
        if (!tokens.isEmpty() && tokens.get(0).equalsIgnoreCase("cancel")) {
            if (chatManager.stopMatchmaking(channel.getId())) {
                channel.sendMessage("Stopped matchmaking").queue();
            } else {
                channel.sendMessage("No matchmaking in progress").queue();
            }
        }
    }
}
