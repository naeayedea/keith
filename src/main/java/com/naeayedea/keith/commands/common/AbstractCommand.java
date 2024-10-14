package com.naeayedea.keith.commands.common;

import com.naeayedea.keith.commands.lib.command.Command;

public abstract class AbstractCommand implements Command {

    private final String defaultName;

    private final int cost;

    public AbstractCommand(String name, int cost) {
        this.defaultName = name;
        this.cost = cost;
    }

    public AbstractCommand(String defaultName) {
        this.defaultName = defaultName;
        this.cost = 1;
    }

    @Override
    public int getTimeOut() {
        return 10;
    }

    @Override
    public boolean sendTyping() {
        return true;
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public int getCost() {
        return cost;
    }
}
