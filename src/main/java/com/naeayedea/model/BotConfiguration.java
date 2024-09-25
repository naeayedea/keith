package com.naeayedea.model;

import javax.sql.DataSource;

public class BotConfiguration {

    private String token;

    private String restartMessage;

    private String restartChannel;

    private DataSource database;

    public BotConfiguration(String token, DataSource database, String restartMessage, String restartChannel) {
        this.token = token;
        this.database = database;
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

    public DataSource getDatabase() {
        return database;
    }

    public void setDatabase(DataSource database) {
        this.database = database;
    }
}
