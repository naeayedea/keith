package com.naeayedea.keith.commands.admin.utilities;

import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.managers.ServerManager;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Clear extends AbstractAdminUtilsCommand {

    private final ServerManager serverManager;

    private final CandidateManager candidateManager;

    public Clear(ServerManager serverManager, CandidateManager candidateManager, @Value("${keith.commands.admin.utilities.clear.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.clear.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
        this.serverManager = serverManager;
        this.candidateManager = candidateManager;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"clear manager caches\"";
    }

    @Override
    public String getLongDescription() {
        return "Used to clear the cache of the candidateManager or ServerManager class\n\n Use ?admin utils clear-cache [server, user, all]";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (tokens.isEmpty()) {
            channel.sendMessage("Clear what cache?").queue();
        } else {
            String type = tokens.remove(0).toLowerCase();
            switch (type) {
                case "server":
                    serverManager.clear();
                    channel.sendMessage("Server cache cleared").queue();
                    break;
                case "user":
                    candidateManager.clear();
                    channel.sendMessage("User cache cleared").queue();
                    break;
                case "all":
                    serverManager.clear();
                    candidateManager.clear();
                    channel.sendMessage("All caches cleared").queue();
                    break;
                default:
                    channel.sendMessage("No valid action supplied").queue();
                    break;
            }
        }
    }
}
