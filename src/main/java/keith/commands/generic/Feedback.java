package keith.commands.generic;

import keith.commands.AccessLevel;
import keith.managers.ServerChatManager;
import keith.managers.ServerManager;
import keith.managers.UserManager;
import keith.util.Utilities;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Feedback extends UserCommand {

    private final String defaultName;
    private final ServerChatManager chatManager;

    public Feedback() {
        defaultName = "feedback";
        chatManager = ServerChatManager.getInstance();
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix+defaultName+": \"bugs or ideas for the bot? use this command to voice your opinion\\\"\"";
    }

    @Override
    public String getLongDescription() {
        return "Have a suggestion? Use this command to contact the bot owner directly!";
    }

    @Override
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        MessageChannel channel = event.getChannel();
        if (channel instanceof PrivateChannel) {
            channel.sendMessage("Feedback not supported in private channels").queue();
            return;
        }
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
                    chatManager.sendMessage(id, event, true);
                } else {
                    //feedback session not in progress, time to create
                    MessageChannel feedbackChannel = chatManager.getFeedbackChannel();
                    Guild guild = event.getGuild();
                    User author = event.getAuthor();
                    String prefix = ServerManager.getInstance().getServer(guild.getId()).getPrefix();
                    //send initial message
                    StringBuilder feedback = new StringBuilder("Initial feedback from ");
                    feedback.append(author.getName()).append(author.getDiscriminator())
                            .append(" in ").append(guild.getName()).append("\n\n")
                            .append("> ").append(Utilities.stringListToString(tokens));
                    if (channel instanceof ThreadChannel) {
                        //create thread inside feedback channel
                        feedbackChannel.sendMessage(feedback).queue(feedbackMessage ->
                                feedbackMessage.createThreadChannel("Feedback from "+guild.getName()).queue(feedbackChannelThread -> {
                                        chatManager.createChat(channel, feedbackChannelThread, false);
                                        channel.sendMessage("Feedback session started - use \""+prefix+"feedback close\" to end the session").queue();
                                })
                        );
                    } else {
                        channel.sendMessage("Please discuss your feedback inside this thread").queue(success ->
                                //create thread inside the users guild
                                success.createThreadChannel("Bot Feedback").queue(feedbackThread -> {
                                    //create thread inside feedback channel
                                    feedbackChannel.sendMessage(feedback).queue(feedbackMessage ->
                                            feedbackMessage.createThreadChannel("Feedback from "+guild.getName()).queue(feedbackChannelThread -> {
                                                chatManager.createChat(feedbackThread, feedbackChannelThread, false);
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
