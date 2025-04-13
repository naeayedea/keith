package com.naeayedea.keith.commands.impl.text.admin;

import com.naeayedea.keith.commands.lib.command.AccessLevel;
import com.naeayedea.keith.managers.KeithUserManager;
import com.naeayedea.keith.managers.ServerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class BanCommand extends AbstractAdminCommand {

    private final ServerManager serverManager;

    private final KeithUserManager keithUserManager;

    private final Logger logger = LoggerFactory.getLogger(BanCommand.class);

    public BanCommand(ServerManager serverManager, KeithUserManager keithUserManager, @Value("${keith.commands.admin.ban.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.ban.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
        this.serverManager = serverManager;
        this.keithUserManager = keithUserManager;
    }

    @NotNull
    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"gives admins the ability to ban users/servers\"";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "bans the specified user or server - do 'ban user/server [user or server id]'";
    }

    @Override
    public void run(@NotNull MessageReceivedEvent event, @NotNull List<String> tokens) {
        try {
            String type = tokens.getFirst();
            List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
            String id;
            if (!mentionedUsers.isEmpty())
                id = mentionedUsers.getFirst().getId();
            else
                id = tokens.get(1);
            if (type.equals("user")) {
                if (keithUserManager.setAccessLevel(id, AccessLevel.ALL).isBanned()) {
                    event.getChannel().sendMessage("User banned").queue();
                } else {
                    event.getChannel().sendMessage("couldn't ban user").queue();
                }
            } else if (type.equals("server") && keithUserManager.getCandidate(event.getAuthor().getId()).getAccessLevel().num > 2) {
                Guild guild = event.getJDA().getGuildById(id);
                if (guild != null) {
                    if (serverManager.setBanned(guild.getId(), true).banned()) {
                        event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Successfully banned server id " + id + " contact succ to undo")).queue();
                    }
                }
            } else {
                event.getChannel().sendMessage("Command unsuccessful, try again").queue();
            }
        } catch (IndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Insufficient arguments, try 'admin help ban' for more assistance").queue();
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage("Invalid formatting, see admin help ban for more information").queue();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);

            event.getChannel().sendMessage("Could not ban server due to SQL Issue.").queue();
        }
    }
}
