package com.naeayedea.keith.common.listener;

import com.naeayedea.keith.common.model.event.KeithEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventListener<T> implements EventListener<T> {

    private final Logger logger = LoggerFactory.getLogger(AbstractEventListener.class);

    //you should probably never override this, its only here as the @Async annotation needs the method to be overridabl
    public void onEvent(KeithEvent<T> eventSource) {
        T event = eventSource.source();

        try {
            if (!serverPermitted(event) || !userPermitted(event) || !eventIsCompatible(event)) {
                onRejected(event);

                return;
            }
        } catch (Throwable e) {
            //don't call the default onError, this is something different, and may cause a side effect of the bot
            //responding to every message it sees, not just commands
            logger.error("Encountered error whilst checking event suitability, reason {}", e.getMessage(), e);
        }

        try {
            onPermitted(event);
        } catch (Throwable e) {
            onError(event, e);
        }
    }
}
