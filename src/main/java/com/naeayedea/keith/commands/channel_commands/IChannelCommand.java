package com.naeayedea.keith.commands.channel_commands;

import com.naeayedea.model.Candidate;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface IChannelCommand {

    void evaluate(Message channel, List<String> message, Candidate candidate);
}
