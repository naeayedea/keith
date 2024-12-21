package com.naeayedea.keith.model;


public record Server(String serverID, String firstSeen, String prefix, Boolean banned, String pinChannel) {

    public String toString() {
       return "<"+serverID+"> First Seen: " + firstSeen + ", Prefix: " + prefix + ", Pin Channel: " + pinChannel;
    }
}