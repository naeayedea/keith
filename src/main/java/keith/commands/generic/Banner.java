package keith.commands.generic;

import keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Banner extends UserCommand {

    public Banner() {
        super("banner");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"Retrieve the profile banner of a user!\"";
    }

    @Override
    public String getLongDescription() {
        return "banner retrieves the profile banner of the user doing the command, alternatively banner can "
                +"retrieve the banner of another user that has been tagged such as ?banner @Succ would return succs banner";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        User user;
        if (!mentionedUsers.isEmpty()) {
            user = mentionedUsers.get(0);
        } else {
            user = event.getAuthor();
        }
        user.retrieveProfile().queue(profile -> {
            String banner = profile.getBannerUrl();
            if (banner != null) {
                EmbedBuilder embed = new EmbedBuilder();
                if (event.getChannel() instanceof PrivateChannel) {
                    embed.setColor(Utilities.getDefaultColor());
                } else {
                    embed.setColor(Utilities.getMemberColor(event.getGuild(), user));
                }
                //Build embed and send
                embed.setTitle(user.getName()+"'s Banner");
                embed.setImage(banner+"?size=4096");
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            } else {
                event.getChannel().sendMessage("User has no banner!").queue();
            }
        });
    }
}
