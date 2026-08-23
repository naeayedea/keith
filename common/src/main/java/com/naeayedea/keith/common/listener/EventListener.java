package com.naeayedea.keith.common.listener;

import com.naeayedea.keith.common.model.event.KeithEvent;

/**
 * Used to implement event listeners in Spring, it is expected that the implementing class
 * override onEvent and annotate with {@link org.springframework.context.event.EventListener} and preferably
 * {@link org.springframework.scheduling.annotation.Async}
 * @param <T> the base class which should trigger the onEvent class
 */
public interface EventListener<T> {

    boolean eventIsCompatible(T event) throws Exception;

    boolean userPermitted(T event) throws Exception;

    boolean serverPermitted(T event) throws Exception;

    void onPermitted(T event) throws Exception;

    void onRejected(T event) throws Exception;

    void onError(T event, Throwable e);

    void onEvent(KeithEvent<T> eventSource);
}
