package com.naeayedea.keith.commands.generic;

import com.naeayedea.keith.managers.ServerChatManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Feedback extends UserCommand {

    private final ServerChatManager chatManager;
    private final ServerManager serverManager;

    public Feedback(ServerChatManager chatManager, ServerManager serverManager) {
        super("feedback");

        this.chatManager = chatManager;
        this.serverManager = serverManager;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+getDefaultName()+": \"bugs or ideas for the bot? use this command to voice your opinion\\\"\"";
    }

    @Override
    public String getLongDescription() {
        return "Have a suggestion? Use this command to contact the bot owner directly!";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        String id = channel.getId();
        boolean active = chatManager.hasActiveChat(id);
        //check if the user is an admin looking to close the feedback session
        if (active && !tokens.isEmpty() && tokens.get(0).equalsIgnoreCase("close")) {
            chatManager.closeChat(channel.getId());
        } else {
            if (tokens.isEmpty()) {
                channel.sendMessage("Please enter a message to send").queue();
            } else {
                if (active) {
                    chatManager.sendMessage(id, event);
                } else {
                    //feedback session not in progress, time to create
                    MessageChannel feedbackChannel = chatManager.getFeedbackChannel();
                    String name;
                    String prefix;
                    if (channel instanceof PrivateChannel) {
                        name = "Private Message";
                        prefix = "?";
                    } else {
                        Guild guild = event.getGuild();
                        prefix = serverManager.getServer(guild.getId()).getPrefix();
                        name = event.getGuild().getName();
                    }
                    User author = event.getAuthor();
                    //send initial message
                    StringBuilder feedback = new StringBuilder("Initial feedback from ");
                    feedback.append(author.getName()).append(author.getDiscriminator())
                            .append(" in ").append(name).append("\n\n")
                            .append("> ").append(Utilities.stringListToString(tokens));
                    if (channel instanceof ThreadChannel || channel instanceof PrivateChannel) {
                        //create thread inside feedback channel
                        feedbackChannel.sendMessage(feedback).queue(feedbackMessage ->
                                feedbackMessage.createThreadChannel("Feedback from "+name).queue(feedbackChannelThread -> {
                                        chatManager.createChat(channel, feedbackChannelThread, true);
                                        channel.sendMessage("Feedback session started - use \""+prefix+"feedback close\" to end the session").queue();
                                })
                        );
                    } else {
                        channel.sendMessage("Please discuss your feedback inside this thread").queue(success ->
                                //create thread inside the users guild
                                success.createThreadChannel("Bot Feedback").queue(feedbackThread -> {
                                    //create thread inside feedback channel
                                    feedbackChannel.sendMessage(feedback).queue(feedbackMessage ->
                                            feedbackMessage.createThreadChannel("Feedback from "+name).queue(feedbackChannelThread -> {
                                                chatManager.createChat(feedbackThread, feedbackChannelThread, true);
                                                feedbackThread.sendMessage("Feedback session started - use \""+prefix+"feedback close\" to end the session").queue();
                                            })
                                    );
                                })
                        );
                    }
                }
            }
        }
    }
}
