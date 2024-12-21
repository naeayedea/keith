package com.naeayedea.keith.commands.lib.command;

import com.naeayedea.keith.model.Candidate;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ChannelCommandDriver {

    void evaluate(@NotNull Message channel, @NotNull List<String> message, @NotNull Candidate candidate);
}
