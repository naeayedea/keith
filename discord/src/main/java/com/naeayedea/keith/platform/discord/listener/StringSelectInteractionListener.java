package com.naeayedea.keith.platform.discord.listener;

import com.naeayedea.keith.core.managers.KeithUserManager;
import com.naeayedea.keith.core.model.event.KeithEvent;
import com.naeayedea.keith.core.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.lib.command.StringSelectInteractionHandler;
import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.managers.ServerManager;
import com.naeayedea.keith.core.util.MultiMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StringSelectInteractionListener extends AbstractUserPermittingDiscordEventListener<StringSelectInteractionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(StringSelectInteractionListener.class);

    private final Map<String, ? extends StringSelectInteractionHandler> handlers;
    private final KeithUserManager keithUserManager;

    @Value("${keith.default-prefix}")
    private String DEFAULT_PREFIX;

    private final ServerManager serverManager;

    public StringSelectInteractionListener(List<? extends StringSelectInteractionHandler> handlers, ServerManager serverManager, KeithUserManager keithUserManager) {
        MultiMap<String, StringSelectInteractionHandler> handlerMap = new MultiMap<>();

        for (StringSelectInteractionHandler handler : handlers) {
            handlerMap.putAll(handler.getTriggerOptions(), handler);
        }

        this.handlers = handlerMap;
        this.serverManager = serverManager;
        this.keithUserManager = keithUserManager;
    }

    @EventListener
    @Async
    @Override
    public void onEvent(KeithEvent<StringSelectInteractionEvent> eventSource) {
        super.onEvent(eventSource);
    }

    @Override
    public void onPermitted(StringSelectInteractionEvent event) throws Exception {
        StringSelectInteractionHandler handler = handlers.get(event.getComponentId());

        if (handler != null) {
            handler.handleStringSelectEvent(event);
        } else {
            logger.warn("No handler found for action: {}", event.getComponentId());

            Guild guild = event.getGuild();

            String prefix = guild == null ? DEFAULT_PREFIX : serverManager.getServer(guild.getId()).getPrefix();

            event.reply("No handler found for this choice. Please try another or use " + prefix + "feedback <message> to get in contact.").queue();
        }
    }

    @Override
    public void onError(StringSelectInteractionEvent event, Throwable e) {
        if (e instanceof KeithExecutionException) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        } else {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected KeithUser getUser(StringSelectInteractionEvent event) {
        return keithUserManager.getUser(event.getUser().getId());
    }

    @Override
    protected boolean isBot(StringSelectInteractionEvent event) {
        return event.getUser().isBot();
    }
}
