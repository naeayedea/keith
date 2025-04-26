package com.naeayedea.keith.platform.discord.listener.events;

public interface KeithTextEvent {

    String getContent();

    String sendResponse(String response);

    KeithChannel getChannel();
}
