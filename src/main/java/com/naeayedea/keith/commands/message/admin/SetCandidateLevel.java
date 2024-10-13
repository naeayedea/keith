package com.naeayedea.keith.commands.message.admin;

import com.naeayedea.keith.commands.message.AccessLevel;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.model.Candidate;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class SetCandidateLevel extends AbstractAdminCommand {

    private final CandidateManager candidateManager;

    private final Logger logger = LoggerFactory.getLogger(SetCandidateLevel.class);

    public SetCandidateLevel(CandidateManager candidateManager, @Value("${keith.commands.admin.setCandidateLevel.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.setCandidateLevel.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
        this.candidateManager = candidateManager;
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"sets the UserLevel of the specified user\"";
    }

    @Override
    public String getDescription() {
        return "sets the UserLevel of the user that corresponds to the entered id or any users that have been tagged" +
            "UserLevels are: 3 (OWNER), 2 (ADMIN), 1 (USER), 0 (BANNED) please use the integer for this command.";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();

        Candidate mentionedCandidate;

        JDA jda = event.getJDA();

        if (mentionedUsers.isEmpty()) {
            if (tokens.size() != 1) {
                channel.sendMessage("Invalid Input, please enter a userid/tag for a single user and an integer corresponding to their level!" +
                    "do help setlevel for more information").queue();
                return;
            } else {
                try {
                    User user = jda.getUserById(tokens.getFirst());
                    if (user != null) {
                        mentionedCandidate = candidateManager.getCandidate(user.getId());
                    } else {
                        channel.sendMessage("Could not locate user").queue();
                        return;
                    }
                } catch (NumberFormatException e) {
                    channel.sendMessage("Invalid user id format").queue();
                    return;
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);

                    channel.sendMessage("Could not locate user").queue();

                    return;
                }
            }
        } else {
            try {
                mentionedCandidate = candidateManager.getCandidate(mentionedUsers.getFirst().getId());
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);

                channel.sendMessage("Could not locate user").queue();

                return;
            }
        }

        try {
            Candidate author = candidateManager.getCandidate(event.getAuthor().getId());

            int newLevel = Integer.parseInt(tokens.get(1));

            if (newLevel < 0 || newLevel > 3) {
                event.getChannel().sendMessage("Invalid level! Use values between 0 (Banned) and 3 (Admin)!").queue();
            } else if (author.getAccessLevel().num > newLevel && author.getAccessLevel().num > mentionedCandidate.getAccessLevel().num) {
                candidateManager.setAccessLevel(mentionedCandidate.getId(), AccessLevel.getLevel("" + newLevel));
                channel.sendMessage("AccessLevel set to " + AccessLevel.getLevel("" + newLevel)).queue();
            } else {
                channel.sendMessage("You do not have the necessary permissions to set this access level!").queue();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Incorrect Formatting! Use format: admin updatelevel [user] [newlevel]").queue();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);

            channel.sendMessage("Could not update access level!").queue();
        }
    }
}
