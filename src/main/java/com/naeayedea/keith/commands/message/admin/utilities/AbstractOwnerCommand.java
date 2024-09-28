package com.naeayedea.keith.commands.message.admin.utilities;

import com.naeayedea.keith.commands.message.AbstractCommand;
import com.naeayedea.keith.commands.message.AccessLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public abstract class AbstractOwnerCommand extends AbstractCommand {

    public AbstractOwnerCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractOwnerCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }

    @Override
    public AccessLevel getAccessLevel() {
        return AccessLevel.OWNER;
    }

}
