package com.naeayedea.keith.platform.discord.command.general.componentTest;

import com.naeayedea.keith.common.model.user.AccessLevel;
import com.naeayedea.keith.common.exception.KeithExecutionException;
import com.naeayedea.keith.common.exception.KeithGracefulErrorException;
import com.naeayedea.keith.common.exception.KeithPermissionException;
import com.naeayedea.keith.platform.discord.command.lib.TextCommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.lang.NonNull;

import java.util.List;

//todo remove me once you know how discord components work in 2025
public class DiscordComponentTestCommandHandler implements TextCommandHandler {
    @Override
    @NonNull
    public AccessLevel getAccessLevel() {
        return AccessLevel.USER;
    }

    @Override
    public int getTimeOut() {
        return 0;
    }

    @Override
    public boolean isPrivateMessageCompatible() {
        return true;
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    @NonNull
    public String getInternalName() {
        return "component-test";
    }

    @Override
    @NonNull
    public String getAliasTranslationKey() {
        return "translation.i18n.commands.component-test.aliases";
    }

    @Override
    @NonNull
    public String getNameTranslationKey() {
        return "translation.i18n.commands.component-test.name";
    }

    @Override
    public void run(@NonNull MessageReceivedEvent event, @NonNull List<String> tokens) throws KeithPermissionException, KeithExecutionException, KeithGracefulErrorException {
        if (tokens.isEmpty()) {
            throw new KeithGracefulErrorException("Expected component type");
        }

        String componentType = tokens.getFirst();

        switch (componentType) {
            case "basic-container" -> {

            }
            default -> throw new KeithExecutionException("Unknown component type: " + componentType);
        }

    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
