package com.naeayedea.keith.platform.discord.listener.interaction;

import com.naeayedea.keith.platform.discord.client.CoreUserClient;
import com.naeayedea.keith.common.model.event.KeithEvent;
import com.naeayedea.keith.common.model.user.KeithUser;
import com.naeayedea.keith.platform.discord.command.lib.StringSelectInteractionHandler;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.platform.discord.server.LocalServerSettingsProvider;
import com.naeayedea.keith.common.util.MultiMap;
import com.naeayedea.keith.platform.discord.listener.AbstractUserPermittingDiscordEventListener;
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
    private final CoreUserClient userClient;

    @Value("${keith.default-prefix}")
    private String DEFAULT_PREFIX;

    private final LocalServerSettingsProvider serverSettings;

    public StringSelectInteractionListener(List<? extends StringSelectInteractionHandler> handlers, LocalServerSettingsProvider serverSettings, CoreUserClient userClient) {
        MultiMap<String, StringSelectInteractionHandler> handlerMap = new MultiMap<>();

        for (StringSelectInteractionHandler handler : handlers) {
            handlerMap.putAll(handler.getTriggerOptions(), handler);
        }

        this.handlers = handlerMap;
        this.serverSettings = serverSettings;
        this.userClient = userClient;
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

        if (handler == null) {
            logger.warn("No handler found for action: {}", event.getComponentId());

            Guild guild = event.getGuild();

            String prefix = guild == null ? DEFAULT_PREFIX : serverSettings.getPrefix(guild.getId());

            event.reply("No handler found for this choice. Please try another or use " + prefix + "feedback <message> to get in contact.").queue();

            return;
        }

        handler.handleStringSelectEvent(event);
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
        return userClient.getOrCreateUser(event.getUser().getId());
    }

    @Override
    protected boolean isBot(StringSelectInteractionEvent event) {
        return event.getUser().isBot();
    }
}
