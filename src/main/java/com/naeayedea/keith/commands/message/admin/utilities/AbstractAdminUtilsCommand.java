package com.naeayedea.keith.commands.message.admin.utilities;

import com.naeayedea.keith.commands.message.admin.AbstractAdminCommand;

import java.util.List;

public abstract class AbstractAdminUtilsCommand extends AbstractAdminCommand {

    public AbstractAdminUtilsCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractAdminUtilsCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }
}
