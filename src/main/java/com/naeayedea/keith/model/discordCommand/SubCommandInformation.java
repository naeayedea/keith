package com.naeayedea.keith.model.discordCommand;

import java.util.List;

public class SubCommandInformation {
    private String name;

    private List<CommandOption> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CommandOption> getOptions() {
        return options;
    }

    public void setOptions(List<CommandOption> options) {
        this.options = options;
    }
}
