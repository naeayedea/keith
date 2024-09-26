package com.naeayedea.keith.commands.admin;

import com.naeayedea.keith.commands.AccessLevel;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.util.Utilities;
import com.naeayedea.model.Candidate;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SetCandidateLevel extends AdminCommand {

    private final CandidateManager candidateManager;

    public SetCandidateLevel(CandidateManager candidateManager) {
        super("setlevel");
        this.candidateManager = candidateManager;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"sets the UserLevel of the specified user\"";
    }

    @Override
    public String getLongDescription() {
        return "sets the UserLevel of the user that corresponds to the entered id or any users that have been tagged" +
            "UserLevels are: 3 (OWNER), 2 (ADMIN), 1 (USER), 0 (BANNED) please use the integer for this command.";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();

        Candidate mentionedCandidate;

        JDA jda = Utilities.getJDAInstance();

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
                }
            }
        } else {
            mentionedCandidate = candidateManager.getCandidate(mentionedUsers.get(0).getId());
        }

        Candidate author = candidateManager.getCandidate(event.getAuthor().getId());

        try {
            int newLevel = Integer.parseInt(tokens.get(1));
            if (newLevel < 0 || newLevel > 3) {
                event.getChannel().sendMessage("Invalid level! Use values between 0 (Banned) and 3 (Admin)!").queue();
            } else if (author.getAccessLevel().num > newLevel && author.getAccessLevel().num > mentionedCandidate.getAccessLevel().num) {
                mentionedCandidate.setAccessLevel(AccessLevel.getLevel("" + newLevel));
                channel.sendMessage("AccessLevel set to " + AccessLevel.getLevel("" + newLevel)).queue();
            } else {
                channel.sendMessage("You do not have the necessary permissions to set this access level!").queue();
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Incorrect Formatting! Use format: admin updatelevel [user] [newlevel]").queue();
        }
    }
}
