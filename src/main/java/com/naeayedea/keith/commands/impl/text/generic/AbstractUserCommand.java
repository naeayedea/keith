package com.naeayedea.keith.commands.impl.text.generic;

import com.naeayedea.keith.commands.impl.text.AbstractTextCommand;
import com.naeayedea.keith.commands.lib.command.AccessLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractUserCommand extends AbstractTextCommand {

    public AbstractUserCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractUserCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @NotNull
    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

}
