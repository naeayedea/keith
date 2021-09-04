package keith.managers;

import keith.commands.channel_commands.ChannelCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ChannelCommandManager {

    private static ChannelCommandManager instance;

    private final Map<String, ChannelCommand> gamesInProgress;

    private ChannelCommandManager() {
        gamesInProgress = new HashMap<>();
    }

    public boolean gameInProgress(String channelId) {
        return gamesInProgress.get(channelId) != null;
    }

    public boolean addGame(String channelId, ChannelCommand newGame) {
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

    public ChannelCommand getGame(String channelId) {
        return gamesInProgress.get(channelId);
    }

    public static ChannelCommandManager getInstance() {
        if (instance == null) {
            instance = new ChannelCommandManager();
        }
        return instance;
    }



}
