package keith.commands.generic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ServerIcon extends UserCommand {

    public ServerIcon() {
        super("servericon");
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"displays icon of the current guild/server\"";
    }

    @Override
    public String getLongDescription() {
        return "servericon displays the icon of the current server/guild";
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return false;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        EmbedBuilder embed = new EmbedBuilder();
        Guild guild = event.getGuild();
        embed.setTitle("Icon for "+guild.getName());
        embed.setImage(guild.getIconUrl()+"?size=4096");
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
