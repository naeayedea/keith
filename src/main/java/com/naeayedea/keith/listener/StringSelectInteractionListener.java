package com.naeayedea.keith.listener;

import com.naeayedea.keith.commands.lib.command.StringSelectInteractionHandler;
import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.MultiMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StringSelectInteractionListener {

    private static final Logger logger = LoggerFactory.getLogger(StringSelectInteractionListener.class);

    private final Map<String, ? extends StringSelectInteractionHandler> handlers;

    @Value("${keith.defaultPrefix}")
    private String DEFAULT_PREFIX;

    private final ServerManager serverManager;

    public StringSelectInteractionListener(List<? extends StringSelectInteractionHandler> handlers, ServerManager serverManager) {
        MultiMap<String, StringSelectInteractionHandler> handlerMap = new MultiMap<>();

        for (StringSelectInteractionHandler handler : handlers) {
            handlerMap.putAll(handler.getTriggerOptions(), handler);
        }

        this.handlers = handlerMap;
        this.serverManager = serverManager;
    }

    @EventListener(StringSelectInteractionEvent.class)
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        StringSelectInteractionHandler handler = handlers.get(event.getComponentId());

        if (handler != null) {
            try {
                handler.handleStringSelectEvent(event);
            } catch (KeithExecutionException e) {
                event.reply(e.getMessage()).setEphemeral(true).queue();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.warn("No handler found for action: {}", event.getComponentId());

            Guild guild = event.getGuild();

            String prefix = guild == null ? DEFAULT_PREFIX : serverManager.getServer(guild.getId()).prefix();

            event.reply("No handler found for this choice. Please try another or use " + prefix + "feedback <message> to get in contact.").queue();
        }

    }
}
