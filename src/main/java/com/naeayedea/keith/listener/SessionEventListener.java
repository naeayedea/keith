package com.naeayedea.keith.listener;

import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.util.Utilities;
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

    private final CandidateManager candidateManager;

    private final ServerManager serverManager;

    private final Logger logger = LoggerFactory.getLogger(SessionEventListener.class);

    public SessionEventListener(CandidateManager candidateManager, ServerManager serverManager) {
        this.candidateManager = candidateManager;
        this.serverManager = serverManager;
    }

    private void onSessionReconnected(JDA jda) {
        logger.info("Session recreated, invalidating caches and updating status.");

        Utilities.updateUptime();
        Utilities.setJDA(jda);

        candidateManager.clear();
        serverManager.clear();

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
