package succ.commands.enhanced;


import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import succ.util.ServerManager;
import succ.util.UserManager;

import java.util.Arrays;
import java.util.List;

/**
 * Bans the specific user from using the bot, bot will completely ignore banned user input.
 */
public class Ban extends EnhancedCommand{

    UserManager userManager;
    ServerManager serverManager;
    public Ban(UserManager userManager, ServerManager serverManager){
        this.userManager=userManager;
        this.serverManager=serverManager;
    }
               @Override
    public String getDescription() {
        return "ban: \"Bans the specified user or server - do '[prefix]ban user/server [user or server id]'\"";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        String commandRaw = event.getMessage().getContentDisplay().toLowerCase().trim();
        String[] commandSplit = commandRaw.split("\\s+");
        try{
            String type = commandSplit[2];
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();
            String id;
            if(mentionedUsers.size()>0)
                id = mentionedUsers.get(0).getId();
            else
                id = commandSplit[3];

            if(type.equals("user")){
                userManager.updateAccessLevel(id, 0);
                event.getChannel().sendMessage("User banned").queue();
                return;
            }
            else if(type.equals("server") && userManager.getUser(event.getAuthor().getId()).getAccessLevel()>2){
                serverManager.banServer(id);
                event.getAuthor().openPrivateChannel().flatMap(channel -> channel.sendMessage("Successfully banned server id "+id+" contact succ to undo")).queue();
                return;
            }
            event.getChannel().sendMessage("Command unsuccesful, try again").queue();
        }
        catch(IndexOutOfBoundsException e){
            event.getChannel().sendMessage("Insufficient arguments, try '"+serverManager.getServer(event.getGuild().getId()).getPrefix()+"admin help' for more assistance").queue();
        }
    }
}
