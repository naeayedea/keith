package com.naeayedea.keith.listener;

import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.Utilities;
import com.naeayedea.keith.model.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class GuildJoinEventListener {

    private final Logger logger = LoggerFactory.getLogger(GuildJoinEventListener.class);

    private final ServerManager serverManager;

    public GuildJoinEventListener(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @EventListener(GuildJoinEvent.class)
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();

        Server server = serverManager.getServer(event.getGuild().getId());

        DefaultGuildChannelUnion defaultChannel = guild.getDefaultChannel();

        if (defaultChannel != null) {
            defaultChannel.asTextChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(new Color(155,0,155))
                .setTitle("Hello!")
                .setFooter("Use "+server.getPrefix()+"feedback if you have any issues!- Succ")
                .setDescription("Use "+server.getPrefix()+"help to see available commands")
                .setThumbnail(Utilities.getJDAInstance().getSelfUser().getAvatarUrl())
                .build()).queue();

            logger.info("New Server {} has added the bot!", guild);
        }
        Utilities.updateDefaultStatus();
    }
}
