package com.naeayedea.keith.commands.impl.text.admin.utilities.restart;

import com.naeayedea.keith.commands.impl.text.admin.utilities.AbstractAdminUtilsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public abstract class BaseRestartCommand extends AbstractAdminUtilsCommand {

    private static final Logger logger = LoggerFactory.getLogger(BaseRestartCommand.class);

    public BaseRestartCommand(@Value("${keith.commands.admin.utilities.restart.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.restart.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"restarts the bot\"";
    }

    @Override
    public String getDescription() {
        return "Will shutdown and relaunch the bots processes - useful after update";
    }


}
