package com.naeayedea.keith.model;

import net.dv8tion.jda.api.entities.Guild;


public record Server(String serverID, String firstSeen, String prefix, Boolean banned, String pinChannel) {

    public String toString() {
       return "<"+serverID+"> First Seen: " + firstSeen + ", Prefix: " + prefix + ", Pin Channel: " + pinChannel;
    }
}