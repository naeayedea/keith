package com.naeayedea.keith.platform.discord.listener.discord;

import com.naeayedea.keith.platform.discord.utils.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SessionEventListener {

    private final Logger logger = LoggerFactory.getLogger(SessionEventListener.class);

    private void onSessionReconnected(JDA jda) {
        logger.info("Session recreated, invalidating caches and updating status.");

        Utilities.updateUptime();
        Utilities.setJDA(jda);

        //user/server data now lives behind core's HTTP API rather than a local cache this app
        //owns, so there's nothing here left to invalidate on reconnect.

        Utilities.updateDefaultStatus();
    }

    @EventListener(SessionRecreateEvent.class)
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        onSessionReconnected(event.getJDA());
    }

    @EventListener(SessionResumeEvent.class)
    public void onSessionRecreate(@NotNull SessionResumeEvent event) {
        onSessionReconnected(event.getJDA());
    }

}
