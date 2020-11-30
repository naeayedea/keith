package succ.commands.generic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.awt.Color;
import java.util.List;

public class Avatar extends UserCommand {
    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "avatar: \"returns the avatar of the user or a mentioned user\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        EmbedBuilder embed = new EmbedBuilder();
        User user;
        if(mentionedUsers.size()>0){
            user = mentionedUsers.get(0);
        }
        else {
            user = event.getAuthor();
        }
        embed.setColor(getColour(event,user));
        embed.setTitle(user.getName()+"'s Avatar");
        embed.setImage(user.getAvatarUrl()+"?size=4096");
        event.getChannel().sendMessage(embed.build()).queue();
    }

    private Color getColour(MessageReceivedEvent event, User user){
        Member member = event.getGuild().getMemberById(user.getId());
        List<Role> roles = member.getRoles();
        if(roles.size()>0)
            return roles.get(0).getColor();
        return new Color(44,47,51);
    }
}
