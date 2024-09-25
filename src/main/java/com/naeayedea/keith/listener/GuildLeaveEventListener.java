package com.naeayedea.keith.listener;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GuildLeaveEventListener {

    private final Logger logger = LoggerFactory.getLogger(GuildLeaveEventListener.class);

    @EventListener(GuildLeaveEvent.class)
    public void onGuildLeave(GuildLeaveEvent event) {
        logger.warn("Server {} has kicked the bot :(", event.getGuild());
        Utilities.updateDefaultStatus();
    }

}
