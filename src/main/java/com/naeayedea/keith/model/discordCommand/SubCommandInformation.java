package com.naeayedea.keith.model.discordCommand;

import java.util.List;

public class SubCommandInformation {
    private String name;

    private String description;

    private List<CommandOption> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CommandOption> getOptions() {
        return options;
    }

    public void setOptions(List<CommandOption> options) {
        this.options = options;
    }
}
