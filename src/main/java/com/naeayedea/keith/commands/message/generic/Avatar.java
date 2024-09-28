package com.naeayedea.keith.commands.message.generic;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Avatar extends AbstractUserCommand {

    public Avatar(@Value("${keith.commands.avatar.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.avatar.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"displays avatar of a user\"";
    }

    @Override
    public String getLongDescription() {
        return "avatar retrieves the current discord avatar of the user doing the command, alternatively avatar can "
            + " retrieve the avatar of another user that has been tagged such as ?avatar @Succ would return succ's avatar";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
        EmbedBuilder embed = new EmbedBuilder();
        User user;
        //Determine if we should return avatar of user or avatar of a tagged user
        if (!mentionedUsers.isEmpty()) {
            user = mentionedUsers.getFirst();
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
        embed.setTitle(user.getName() + "'s Avatar");
        embed.setImage(user.getAvatarUrl() + "?size=4096");
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

}
