package com.naeayedea.keith.core.commands.lib.output.basic;

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
