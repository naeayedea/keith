package com.naeayedea.keith.platform.discord.listener;

import net.dv8tion.jda.api.events.Event;

public abstract class AbstractKeithListenerEvent<T extends Event> implements KeithEventListener<T> {


    @Override
    public boolean eventIsCompatible(T event) {
        return true;
    }

    @Override
    public boolean userPermitted(T event) {
        return true;
    }

    @Override
    public boolean serverPermitted(T event) {
        return true;
    }

    @Override
    public void onPermitted(T event) {

    }

    @Override
    public void onRejected(T event) {

    }

    @Override
    public void onError(T event) {

    }

    public final void onEvent(T event) {

    }
}
