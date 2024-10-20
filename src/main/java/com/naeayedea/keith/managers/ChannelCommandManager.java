package com.naeayedea.keith.managers;

import com.naeayedea.keith.commands.impl.text.channelCommandDrivers.ChannelCommandDriver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChannelCommandManager {

    private final Map<String, ChannelCommandDriver> gamesInProgress;

    public ChannelCommandManager() {
        gamesInProgress = new HashMap<>();
    }

    public boolean gameInProgress(String channelId) {
        return gamesInProgress.get(channelId) != null;
    }

    public boolean addGame(String channelId, ChannelCommandDriver newGame) {
        if (gameInProgress(channelId)) {
            return false;
        } else {
            gamesInProgress.put(channelId, newGame);
            return true;
        }
    }

    public void removeGame(String channelId) {
        gamesInProgress.remove(channelId);
    }

    public ChannelCommandDriver getGame(String channelId) {
        return gamesInProgress.get(channelId);
    }

}
