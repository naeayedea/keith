package com.naeayedea.keith.model;

import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.entities.Guild;


public record Server(String serverID, String firstSeen, String prefix, Boolean banned, String pinChannel) {

    public String toString() {
        Guild guild = Utilities.getJDAInstance().getGuildById(this.serverID);
        String tail = "> First Seen: " + firstSeen + ", Prefix: " + prefix + ", Pin Channel: " + pinChannel;
        return guild == null ? "Unknown Server, ID: " + serverID + tail : guild.getName() + "<" + guild.getId() + tail;
    }
}