package com.naeayedea.keith.platform.discord.listener;

import net.dv8tion.jda.api.events.Event;

public interface KeithEventListener<T extends Event> {

    boolean eventIsCompatible(T event);

    boolean userPermitted(T event);

    boolean serverPermitted(T event);

    void onPermitted(T event);

    void onRejected(T event);

    void onError(T event);
}
