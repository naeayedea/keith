package com.naeayedea.keith.common.model.command.output.basic;

public class SimpleTextCommandOutput implements TextCommandOutput {

    private final String text;

    public SimpleTextCommandOutput(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
}
