package com.naeayedea.keith.commands.text.generic;

import com.naeayedea.keith.commands.text.AbstractCommand;
import com.naeayedea.keith.commands.text.AccessLevel;

import java.util.List;

public abstract class AbstractUserCommand extends AbstractCommand {

    public AbstractUserCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractUserCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
