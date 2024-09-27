package com.naeayedea.keith.commands.channelCommandDrivers;

import com.naeayedea.keith.model.Candidate;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface ChannelCommandDriver {

    void evaluate(Message channel, List<String> message, Candidate candidate);
}
