package com.naeayedea.keith.core.commands.drivers.general.ping;

import com.naeayedea.keith.core.commands.lib.BaseCommandMessages;
import com.naeayedea.keith.core.commands.lib.strategy.CommandStrategy;

public interface PingCommandStrategy extends CommandStrategy {

    interface PingCommandMessages extends BaseCommandMessages {
        String PING_RESPONSE = "translation.i18n.commands.ping.messages.message-received-after";
    }
}
