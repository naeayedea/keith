package succ.commands.generic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Avatar extends UserCommand {
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "avatar: \"returns the avatar of the user or a mentioned user\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        EmbedBuilder embed = new EmbedBuilder().setColor(new Color(155,0,155));
        if(mentionedUsers.size()>0){
            User user = mentionedUsers.get(0);
            embed.setTitle(user.getName()+"'s Avatar");
            embed.setImage(user.getAvatarUrl());
        }
        else {
            User user = event.getAuthor();
            embed.setTitle(user.getName()+"'s Avatar");
            embed.setImage(user.getAvatarUrl());
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }
}
