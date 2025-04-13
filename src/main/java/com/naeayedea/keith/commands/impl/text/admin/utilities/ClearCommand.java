package com.naeayedea.keith.commands.impl.text.admin.utilities;

import com.naeayedea.keith.managers.KeithUserManager;
import com.naeayedea.keith.managers.ServerManager;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClearCommand extends AbstractAdminUtilsCommand {

    private final ServerManager serverManager;

    private final KeithUserManager keithUserManager;

    public ClearCommand(ServerManager serverManager, KeithUserManager keithUserManager, @Value("${keith.commands.admin.utilities.clear.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.clear.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
        this.serverManager = serverManager;
        this.keithUserManager = keithUserManager;
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"clear manager caches\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Used to clear the cache of the keithUserManager or ServerManager class\n\n Use ?admin utils clear-cache [server, user, all]";
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (tokens.isEmpty()) {
            channel.sendMessage("Clear what cache?").queue();
        } else {
            String type = tokens.removeFirst().toLowerCase();
            switch (type) {
                case "server":
                    serverManager.clear();
                    channel.sendMessage("Server cache cleared").queue();
                    break;
                case "user":
                    keithUserManager.clear();
                    channel.sendMessage("User cache cleared").queue();
                    break;
                case "all":
                    serverManager.clear();
                    keithUserManager.clear();
                    channel.sendMessage("All caches cleared").queue();
                    break;
                default:
                    channel.sendMessage("No valid action supplied").queue();
                    break;
            }
        }
    }
}
