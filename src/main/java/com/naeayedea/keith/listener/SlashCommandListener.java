package com.naeayedea.keith.listener;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandListener {

    @EventListener(SlashCommandInteractionEvent.class)
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "say" -> {
                String content = event.getOption("message", OptionMapping::getAsString);

                event.reply(content != null ? content : "What do you want me to say?")
                    .setEphemeral(true)
                    .queue();
            }
            case "testwithgroups" -> {
                String chosenGroup = event.getSubcommandGroup();

                if ("groupone".equals(chosenGroup)) {
                    String chosenSubCommand = event.getSubcommandName();

                    if ("say".equals(chosenSubCommand)) {
                        String content = event.getOption("message", OptionMapping::getAsString);

                        event.reply(content != null ? content : "What do you want me to say?")
                            .setEphemeral(true)
                            .queue();
                    } else if ("saywithoptions".equals(chosenSubCommand)) {
                        String content = event.getOption("message", OptionMapping::getAsString);

                        event.reply(content != null ? content : "What do you want me to say?")
                            .setEphemeral(true)
                            .queue();
                    } else {
                        event.reply("No subcommand found in group one")
                            .setEphemeral(true)
                            .queue();
                    }

                } else if ("grouptwo".equals(chosenGroup)) {
                    String chosenSubCommand = event.getSubcommandName();

                    if ("sayhello".equals(chosenSubCommand)) {
                        event.reply("Hello")
                            .setEphemeral(true)
                            .queue();
                    } else {
                        event.reply("No subcommand found in group two")
                            .setEphemeral(true)
                            .queue();
                    }

                } else {
                    event.reply("No group found.")
                        .setEphemeral(true)
                        .queue();
                }
            }
            default -> event.reply("No handler")
                .setEphemeral(true)
                .queue();
        }
    }
}
