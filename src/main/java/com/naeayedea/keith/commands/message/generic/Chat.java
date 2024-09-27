package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.managers.ServerChatManager;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Chat extends AbstractUserCommand {


    private final ServerChatManager chatManager;

    public Chat(ServerChatManager chatManager, @Value("${keith.commands.chat.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.chat.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);

        this.chatManager = chatManager;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"Use '"+prefix+"chat start' to connect to another server for a quick chat!\"";
    }

    @Override
    public String getLongDescription() {
        return "Creates a connection between two guilds so that messages can be sent in between. " +
                "Use your servers prefix before a message to avoid the message being sent!";
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
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
