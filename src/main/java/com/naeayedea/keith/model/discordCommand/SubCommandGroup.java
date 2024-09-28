package com.naeayedea.keith.model.discordCommand;

import java.util.List;

public class SubCommandGroup {

    private String name;

    private List<SubCommandInformation> subCommands;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SubCommandInformation> getSubCommands() {
        return subCommands;
    }

    public void setSubCommands(List<SubCommandInformation> subCommands) {
        this.subCommands = subCommands;
    }
}
