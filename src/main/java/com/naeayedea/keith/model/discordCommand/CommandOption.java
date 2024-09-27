package com.naeayedea.keith.model.discordCommand;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

public class CommandOption {

    private String name;

    private String description;

    private String type;

    private boolean isOptional;

    private List<CommandChoice> choices;

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

    @JsonSetter("isOptional")
    public boolean isOptional() {
        return isOptional;
    }

    @JsonSetter("isOptional")
    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public List<CommandChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<CommandChoice> choices) {
        this.choices = choices;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
