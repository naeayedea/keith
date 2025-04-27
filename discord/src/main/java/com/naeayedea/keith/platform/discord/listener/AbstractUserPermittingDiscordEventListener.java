package com.naeayedea.keith.platform.discord.listener;

import com.naeayedea.keith.core.model.user.KeithUser;
import net.dv8tion.jda.api.events.Event;

public abstract class AbstractUserPermittingDiscordEventListener<T extends Event> extends AbstractDiscordEventListener<T> {

    protected abstract KeithUser getUser(T event);

    protected abstract boolean isBot(T event);

    @Override
    public boolean userPermitted(T event) {
        if (isBot(event)) {
            return false;
        }

        KeithUser user = getUser(event);

        return !user.isBanned();
    }
}
