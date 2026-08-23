package com.naeayedea.keith.platform.discord.listener.guild;

import com.naeayedea.keith.common.model.event.KeithEvent;
import com.naeayedea.keith.platform.discord.listener.AbstractDiscordEventListener;
import com.naeayedea.keith.platform.discord.server.LocalServerSettingsProvider;
import com.naeayedea.keith.platform.discord.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class GuildJoinEventEventListener extends AbstractDiscordEventListener<GuildJoinEvent> {

    private final Logger logger = LoggerFactory.getLogger(GuildJoinEventEventListener.class);

    private final LocalServerSettingsProvider serverSettings;

    public GuildJoinEventEventListener(LocalServerSettingsProvider serverSettings) {
        this.serverSettings = serverSettings;
    }

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<GuildJoinEvent> eventSource) {
        logger.info("event received: {}", eventSource.source());

        super.onEvent(eventSource);
    }

    @Override
    public boolean serverPermitted(GuildJoinEvent event) {
        return !serverSettings.isBanned(event.getGuild().getId());
    }

    @Override
    public void onPermitted(GuildJoinEvent event) {
        Guild guild = event.getGuild();

        String prefix = serverSettings.getPrefix(event.getGuild().getId());

        DefaultGuildChannelUnion defaultChannel = guild.getDefaultChannel();

        Utilities.updateDefaultStatus();

        logger.info("New Server {} has added the bot!", guild);

        if (defaultChannel == null) {
            return;
        }

        defaultChannel.asTextChannel().sendMessageEmbeds(new EmbedBuilder()
            .setColor(new Color(155, 0, 155))
            .setTitle("Hello!")
            .setFooter("Use " + prefix + "feedback if you have any issues!- Succ")
            .setDescription("Use " + prefix + "help to see available commands")
            .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
            .build()).queue();

    }

    @Override
    public void onRejected(GuildJoinEvent event) {
        logger.warn("A banned server has added the bot, leaving. Id: {}", event.getGuild().getId());

        //leave the server again
        event.getGuild().leave().complete();
    }
}
