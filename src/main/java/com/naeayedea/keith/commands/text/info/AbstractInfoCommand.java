package com.naeayedea.keith.commands.text.info;

import com.naeayedea.keith.commands.text.AbstractCommand;
import com.naeayedea.keith.commands.text.AccessLevel;

import java.util.List;

public abstract class AbstractInfoCommand extends AbstractCommand {

    public AbstractInfoCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractInfoCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.ALL;
    }

}
