package com.naeayedea.keith.commands.message.admin;

import com.naeayedea.keith.commands.message.AccessLevel;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Ban extends AbstractAdminCommand {

    private final ServerManager serverManager;

    private final CandidateManager candidateManager;

    public Ban(ServerManager serverManager, CandidateManager candidateManager, @Value("${keith.commands.admin.ban.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.ban.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
        this.serverManager = serverManager;
        this.candidateManager = candidateManager;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"gives admins the ability to ban users/servers\"";
    }

    @Override
    public String getLongDescription() {
        return "bans the specified user or server - do 'ban user/server [user or server id]'";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        try{
            String type = tokens.get(0);
            List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
            String id;
            if (mentionedUsers.size() > 0)
                id = mentionedUsers.get(0).getId();
            else
                id = tokens.get(1);
            if (type.equals("user")) {
                if (candidateManager.getCandidate(id).setAccessLevel(AccessLevel.ALL)) {
                    event.getChannel().sendMessage("User banned").queue();
                } else {
                    event.getChannel().sendMessage("couldn't ban user").queue();
                }
            } else if(type.equals("server") && candidateManager.getCandidate(event.getAuthor().getId()).getAccessLevel().num > 2){
                Guild guild = Utilities.getJDAInstance().getGuildById(id);
                if (guild != null) {
                    if (serverManager.getServer(guild.getId()).setBanned(true)) {
                        event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Successfully banned server id "+id+" contact succ to undo")).queue();
                    }
                }
            } else {
                event.getChannel().sendMessage("Command unsuccessful, try again").queue();
            }
        }
        catch (IndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Insufficient arguments, try 'admin help ban' for more assistance").queue();
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage("Invalid formatting, see admin help ban for more information").queue();
        }
    }
}
