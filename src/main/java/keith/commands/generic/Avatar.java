package keith.commands.generic;

import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class Avatar extends UserCommand {

    String defaultName;

    public Avatar() {
        defaultName = "avatar";
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"displays avatar of a user\"";
    }

    @Override
    public String getLongDescription() {
        return "avatar retrieves the current discord avatar of the user doing the command, alternatively avatar can "
                +" retrieve the avatar of another user that has been tagged such as ?avatar @Succ would return succs avatar";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        EmbedBuilder embed = new EmbedBuilder();
        User user;
        //Determine if we should return avatar of user or avatar of a tagged user
        if (!mentionedUsers.isEmpty()) {
            user = mentionedUsers.get(0);
        } else {
            user = event.getAuthor();
        }
        //Retrieve the colour of the user in question
        if (event.getChannel() instanceof PrivateChannel) {
            embed.setColor(Utilities.getDefaultColor());
        } else {
        embed.setColor(Utilities.getMemberColor(event.getGuild(), user));
        }
        //Build embed and send
        embed.setTitle(user.getName()+"'s Avatar");
        embed.setImage(user.getAvatarUrl()+"?size=4096");
        event.getChannel().sendMessage(embed.build()).queue();
    }

}
