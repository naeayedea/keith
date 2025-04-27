package com.naeayedea.keith.platform.discord.listener.interaction;

import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.listener.AbstractUserPermittingDiscordEventListener;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSlashCommandEventListener<T extends GenericCommandInteractionEvent> extends AbstractUserPermittingDiscordEventListener<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSlashCommandEventListener.class);

    @Override
    public void onError(T event, Throwable e) {
        if (e instanceof KeithGracefulErrorException) {
            event
                .reply(e.getMessage())
                .setEphemeral(true)
                .queue();
        } else if (e instanceof KeithPermissionException) {
            event.reply("You do not have permission to do that!")
                .setEphemeral(true)
                .queue();
        } else if (e != null) {
            event.reply("Something went wrong :(")
                .setEphemeral(true)
                .queue();

            logger.error("Error encountered whilst running command {}, {}", event.getName(), e.getMessage(), e);
        }
    }

}
