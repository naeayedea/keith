package com.naeayedea.keith.platform.discord.config;

public class DiscordConfiguration {

    private String token;

    private String restartMessage;

    private String restartChannel;

    public DiscordConfiguration(String token, String restartMessage, String restartChannel) {
        this.token = token;
        this.restartMessage = restartMessage;
        this.restartChannel = restartChannel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRestartMessage() {
        return restartMessage;
    }

    public void setRestartMessage(String restartMessage) {
        this.restartMessage = restartMessage;
    }

    public String getRestartChannel() {
        return restartChannel;
    }

    public void setRestartChannel(String restartChannel) {
        this.restartChannel = restartChannel;
    }
}
