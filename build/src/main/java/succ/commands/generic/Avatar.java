package succ.commands.generic;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Avatar extends UserCommand {
    @Override
    public String getDescription() {
        return "avatar: \"Returns the avatar of the user or a mentioned user\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        if(mentionedUsers.size()>0){
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + " "+ mentionedUsers.get(0).getAvatarUrl()).queue();
        }
        else {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + " " + event.getAuthor().getAvatarUrl()).queue();
        }
    }
}
