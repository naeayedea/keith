package com.naeayedea.keith.platform.discord.listener.guild;

import com.naeayedea.keith.core.model.event.KeithEvent;
import com.naeayedea.keith.platform.discord.listener.AbstractDiscordEventListener;
import com.naeayedea.keith.platform.discord.utils.Utilities;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class GuildLeaveEventEventListener extends AbstractDiscordEventListener<GuildLeaveEvent> {

    private final Logger logger = LoggerFactory.getLogger(GuildLeaveEventEventListener.class);

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<GuildLeaveEvent> eventSource) {
        super.onEvent(eventSource);
    }

    @Override
    public void onPermitted(GuildLeaveEvent event) {
        logger.info("Keith is no longer in guild {}", event.getGuild());

        Utilities.updateDefaultStatus();
    }

    @Override
    public void onRejected(GuildLeaveEvent event) {

    }

    @Override
    public void onError(GuildLeaveEvent event, Throwable e) {

    }
}
