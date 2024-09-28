package com.naeayedea.keith.commands.message.info;

import com.naeayedea.keith.commands.message.AbstractCommand;
import com.naeayedea.keith.commands.message.AccessLevel;

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
