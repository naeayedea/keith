package com.naeayedea.keith.commands.text.admin.utilities;

import com.naeayedea.keith.commands.text.admin.AbstractAdminTextCommand;

import java.util.List;

public abstract class AbstractAdminUtilsTextCommand extends AbstractAdminTextCommand {

    public AbstractAdminUtilsTextCommand(String name, List<String> commandAliases) {
        super(name, commandAliases);
    }

    public AbstractAdminUtilsTextCommand(String name, List<String> commandAliases, boolean isPrivateMessageCompatible, boolean isHidden) {
        super(name, commandAliases, isPrivateMessageCompatible, isHidden);
    }
}
