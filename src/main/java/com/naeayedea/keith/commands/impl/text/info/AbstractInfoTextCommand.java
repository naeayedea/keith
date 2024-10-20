package com.naeayedea.keith.commands.impl.text.info;

import com.naeayedea.keith.commands.impl.text.AbstractTextCommand;
import com.naeayedea.keith.commands.lib.command.AccessLevel;

import java.util.List;

public abstract class AbstractInfoTextCommand extends AbstractTextCommand {

    public AbstractInfoTextCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractInfoTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }

}
