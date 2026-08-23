package com.naeayedea.keith.platform.discord.listener;

import com.naeayedea.keith.common.listener.AbstractEventListener;
import net.dv8tion.jda.api.events.Event;

public abstract class AbstractDiscordEventListener<T extends Event> extends AbstractEventListener<T> {

    @Override
    public boolean eventIsCompatible(T event) throws Exception {
        return true;
    }

    @Override
    public boolean userPermitted(T event) throws Exception {
        return true;
    }

    @Override
    public boolean serverPermitted(T event) throws Exception {
        return true;
    }

    @Override
    public void onRejected(T event) throws Exception {

    }

    @Override
    public void onError(T event, Throwable e) {

    }
}
