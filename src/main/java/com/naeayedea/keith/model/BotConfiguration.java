package com.naeayedea.keith.model;

import javax.sql.DataSource;

public class BotConfiguration {

    private String token;

    private String restartMessage;

    private String restartChannel;

    private DataSource dataSource;

    public BotConfiguration(String token, DataSource dataSource, String restartMessage, String restartChannel) {
        this.token = token;
        this.dataSource = dataSource;
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

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
