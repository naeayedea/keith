package com.naeayedea.keith.commands.impl.text.generic;

import com.naeayedea.keith.managers.ServerChatManager;
import com.naeayedea.keith.managers.ServerManager;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatTextCommand extends AbstractUserTextCommand {

    private final ServerManager serverManager;

    private final ServerChatManager chatManager;

    public ChatTextCommand(ServerManager serverManager, ServerChatManager chatManager, @Value("${keith.commands.chat.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.chat.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
        this.serverManager = serverManager;

        this.chatManager = chatManager;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": Use '" + prefix + "chat start' to connect to another server for a quick chat!";
    }

    @Override
    public String getDescription() {
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

        boolean active = chatManager.hasActiveChat(channel.getId());
        if (!active) {
            //no active chat
            if (tokens.size() == 1) {
                switch (tokens.getFirst()) {
                    case "start" -> {
                        if (chatManager.isInMatchmaking(channel.getId())) {
                            channel.sendMessage("Matchmaking in progress (This may take some time!").queue();
                        } else if (!chatManager.startMatchmaking(channel, event.getGuild())) {
                            //start matchmaking, if returns false then nobody else in the queue
                            channel.sendMessage("Matchmaking started (Could take a while)").queue();
                        }
                    }
                    case "cancel" -> {
                        if (chatManager.stopMatchmaking(channel.getId())) {
                            channel.sendMessage("Stopped matchmaking").queue();
                        } else {
                            channel.sendMessage("No matchmaking in progress").queue();
                        }
                    }
                    default -> event.getMessage().reply("Use start to search for a chat partner, or cancel to stop the current search").queue();
                }
            } else {
                event.getMessage().reply("Use start to search for a chat partner, or cancel to stop the current search").queue();
            }
        } else {
            //we have an active chat
            if (tokens.isEmpty()) {
                event.getMessage().reply("Use close to stop the current chat session. If you want to send a message to connected server just type!").queue();
            } else if (tokens.size() == 1 && tokens.getFirst().equals("close")) {
                chatManager.closeChat(channel.getId());
            } else {
                event.getMessage().reply("You don't need to use the command to send a message, simply type! If you want to stop the chat then use \""+serverManager.getServer(event.getGuild().getId()).prefix()+"chat close\"").queue();
            }
        }
    }
}
