package com.naeayedea.keith.model.discordCommand;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommandInformation {

    private String name;

    private boolean isNSFW;

    private boolean isGuildOnly;

    private String type;

    private String defaultPermission;

    private List<CommandOption> options;

    private List<SubCommandInformation> subCommands;

    private List<SubCommandGroup> subCommandGroups;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter("isNSFW")
    public boolean isNSFW() {
        return isNSFW;
    }

    @JsonSetter("isNSFW")
    public void setNSFW(boolean NSFW) {
        isNSFW = NSFW;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<CommandOption> getOptions() {
        return options;
    }

    public void setOptions(List<CommandOption> options) {
        this.options = options;
    }

    public boolean isGuildOnly() {
        return isGuildOnly;
    }

    public void setGuildOnly(boolean guildOnly) {
        isGuildOnly = guildOnly;
    }

    public List<SubCommandGroup> getSubCommandGroups() {
        return subCommandGroups;
    }

    public void setSubCommandGroups(List<SubCommandGroup> subCommandGroups) {
        this.subCommandGroups = subCommandGroups;
    }

    public List<SubCommandInformation> getSubCommands() {
        return subCommands;
    }

    public void setSubCommands(List<SubCommandInformation> subCommands) {
        this.subCommands = subCommands;
    }

    public String getDefaultPermission() {
        return defaultPermission;
    }

    public void setDefaultPermission(String defaultPermission) {
        this.defaultPermission = defaultPermission;
    }
}
