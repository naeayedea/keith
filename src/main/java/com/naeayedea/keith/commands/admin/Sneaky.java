package com.naeayedea.keith.commands.admin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import java.awt.*;
import java.util.List;

/**
 * Sneaky is a command which attempts to give any bot admin a role
 * with server admin if the bot has sufficient permissions. (only for fun in friend servers)
 */
public class Sneaky extends AdminCommand {

    public Sneaky() {
        super("sneaky", false, true);
    }

    @Override
    public String getShortDescription(String prefix) {
        return "sneaky: \"attempts to give any bot admin a role with server admin if the bot has permissions\"";
    }

    @Override
    public String getLongDescription() {
        return "Attempts to give admin powers to the user, will only work if keith has higher permissions than the user and also has admin powers";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        try{
            Guild guild = event.getGuild();
            RoleAction roleBuilder = guild.createRole()
                    .setName("sneaky")
                    .setPermissions(8L)
                    .setColor(new Color(155,0,155));
            //Assign role and delete original message for sneak
            Member target = event.getMember();
            if (target != null) {
                guild.addRoleToMember(target, roleBuilder.complete()).queue();
                event.getMessage().delete().queue();
            } else {
                event.getChannel().sendMessage("couldn't").queue(message -> event.getMessage().delete().queue());
            }
        }
        catch(InsufficientPermissionException e){
            event.getChannel().sendMessage("couldn't assign new role, insufficient permissions").queue();
        }
    }

}