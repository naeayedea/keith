package com.naeayedea.keith.core.service;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * The list of commands core exposes over HTTP. There's no bean-scanning/auto-discovery here
 * deliberately - with two commands, an explicit list is simpler and more honest than a framework
 * for a problem this small; revisit once there are enough commands for that tradeoff to flip.
 *
 * @author naeayedea
 */
@Component
public class CommandRegistry {

    /**
     * All commands core currently exposes, in display order.
     */
    private final List<CommandDescriptor> commands = List.of(
        new CommandDescriptor(
            "ping",
            "translation.i18n.commands.ping.name",
            "translation.i18n.commands.ping.aliases",
            "translation.i18n.commands.ping.desc.text"
        ),
        new CommandDescriptor(
            "help",
            "translation.i18n.commands.help.name",
            "translation.i18n.commands.help.aliases",
            "translation.i18n.commands.help.desc.text"
        ),
        new CommandDescriptor(
            "pin",
            "translation.i18n.commands.pin.name",
            "translation.i18n.commands.pin.aliases",
            "translation.i18n.commands.pin.desc.text"
        ),
        new CommandDescriptor(
            "guess",
            "translation.i18n.commands.guess.name",
            "translation.i18n.commands.guess.aliases",
            "translation.i18n.commands.guess.desc.text"
        ),
        new CommandDescriptor(
            "chat",
            "translation.i18n.commands.chat.name",
            "translation.i18n.commands.chat.aliases",
            "translation.i18n.commands.chat.desc.text"
        )
    );

    @NonNull
    public List<CommandDescriptor> getCommands() {
        return commands;
    }

    @NonNull
    public Optional<CommandDescriptor> findByInternalName(@NonNull String internalName) {
        return commands.stream()
            .filter(command -> command.internalName().equals(internalName))
            .findFirst();
    }
}
