package com.naeayedea.keith.commands.text.admin.utilities;

import com.naeayedea.keith.commands.text.AbstractTextCommand;
import com.naeayedea.keith.commands.text.AccessLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public abstract class AbstractOwnerTextCommand extends AbstractTextCommand {

    public AbstractOwnerTextCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractOwnerTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

}
