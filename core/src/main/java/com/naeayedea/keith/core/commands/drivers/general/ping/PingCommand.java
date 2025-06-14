package com.naeayedea.keith.core.commands.drivers.general.ping;

import com.naeayedea.keith.core.commands.lib.AbstractChannelCommand;
import com.naeayedea.keith.core.commands.lib.AccessLevel;
import com.naeayedea.keith.core.commands.lib.input.TextCommandInput;
import com.naeayedea.keith.core.commands.lib.output.basic.SimpleTextCommandOutput;
import com.naeayedea.keith.core.commands.lib.output.basic.TextCommandOutput;
import com.naeayedea.keith.core.exception.KeithExecutionException;
import com.naeayedea.keith.core.exception.KeithGracefulErrorException;
import com.naeayedea.keith.core.exception.KeithPermissionException;
import com.naeayedea.keith.core.i18n.TranslationProvider;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.time.Instant;

import static com.naeayedea.keith.core.commands.drivers.general.ping.PingCommandStrategy.PingCommandMessages.PING_RESPONSE;

public class PingCommand extends AbstractChannelCommand<TextCommandInput, TextCommandOutput, PingCommandStrategy> {

    public PingCommand(String internalName, String nameKey, String aliasKey) {
        super(internalName, nameKey, aliasKey);
    }

    @Override
    @NonNull
    public TextCommandOutput run(@NonNull TextCommandInput input, @NonNull PingCommandStrategy strategy) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        TranslationProvider translationProvider = strategy.getTranslationProvider();

        long timeUntilReceivingMessage = Duration.between(input.getTimestamp(), Instant.now()).abs().toMillis();

        return new SimpleTextCommandOutput(translationProvider.getTranslation(PING_RESPONSE, new Object[]{timeUntilReceivingMessage, }, input.getLocale()));
    }

    @Override
    @NonNull
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return true;
    }
}
