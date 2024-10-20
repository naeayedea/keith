package com.naeayedea.keith.commands.impl.text.generic;

import com.naeayedea.keith.commands.impl.text.AbstractTextCommand;
import com.naeayedea.keith.commands.lib.command.AccessLevel;

import java.util.List;

public abstract class AbstractUserTextCommand extends AbstractTextCommand {

    public AbstractUserTextCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractUserTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
