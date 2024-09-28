package com.naeayedea.keith.commands.message.admin;

import com.naeayedea.keith.exception.KeithExecutionException;
import com.naeayedea.keith.managers.CandidateManager;
import com.naeayedea.keith.model.Candidate;
import com.naeayedea.keith.util.Database;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class Stats extends AbstractAdminCommand {

    private static final String VERSION = "3.1.1 \"PIN HOTFIX\"";

    private final CandidateManager candidateManager;

    private final Database database;

    @Value("${keith.commands.stats.statements.returnAdmins}")
    private String RETURN_ADMINS_STATEMENT;

    @Value("${keith.commands.stats.statements.returnServers}")
    private String RETURN_SERVER_STATEMENT;

    public Stats(CandidateManager candidateManager, @Value("${keith.commands.admin.stats.defaultName}") String defaultName, @Value("#{T(com.naeayedea.converter.StringToAliasListConverter).convert('${keith.commands.admin.stats.aliases}', ',')}") List<String> commandAliases, Database database) {
        super(defaultName, commandAliases);
        this.candidateManager = candidateManager;
        this.database = database;
    }

    @Override
    public String getShortDescription(String prefix) {
        return prefix + getDefaultName() + ": \"returns various bot stats from database\"";
    }

    @Override
    public String getLongDescription() {
        return "usage: admin stats (servers/users/admins)";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) throws KeithExecutionException {
        JDA jda = Utilities.getJDAInstance();
        MessageChannel channel = event.getChannel();

        String type;
        if (tokens.isEmpty()) {
            sendStats(channel);
        } else {
            type = tokens.getFirst();

            switch (type.toLowerCase()) {
                case "admins" -> {
                    List<String> results = database.getStringResult(RETURN_ADMINS_STATEMENT);

                    StringBuilder adminList = new StringBuilder();

                    for (int i = 1; i < results.size(); i++) {
                        String discordId = results.get(i).trim();

                        try {
                            Candidate candidate = candidateManager.getCandidate(discordId);

                            adminList.append("> ").append(candidate.getDescription()).append("\n");
                        } catch (SQLException e) {
                            throw new KeithExecutionException(e);
                        }
                    }

                    channel.sendMessage("Admin Users:\n" + adminList).queue();
                }
                case "servers" -> {
                    List<String> results = database.getStringResult(RETURN_SERVER_STATEMENT);

                    StringBuilder serverList = new StringBuilder();

                    for (int i = 1; i < results.size(); i++) {
                        String[] args = results.get(i).split("\\s+");

                        String serverId = args[0].trim();
                        String firstSeen = args[1];

                        Guild server = jda.getGuildById(serverId);

                        if (server != null)
                            serverList.append("> ").append(server.getName()).append("(").append(server.getId()).append(") First Seen: ").append(firstSeen).append("\n");
                    }
                    channel.sendMessage("Servers:\n" + serverList).queue();

                }
                case "users" -> channel.sendMessage("User count: " + returnUserCount()).queue();
                default -> channel.sendMessage("Expected admins or servers, got: "+type).queue();
            }
        }
    }

    private int returnUserCount() {
        return Utilities.getJDAInstance().getUsers().size();
    }

    private void sendStats(MessageChannel channel) {
        JDA jda = Utilities.getJDAInstance();
        EmbedBuilder bd = new EmbedBuilder();
        bd.setTitle("Stats");
        bd.setAuthor(jda.getSelfUser().getName() + " Version " + VERSION, null, jda.getSelfUser().getAvatarUrl());
        bd.setColor(Utilities.getBotColor());
        bd.addField("Uptime", Utilities.getUptimeString(), false);
        bd.addField("Total Users", "" + returnUserCount(), true);
        bd.addField("Total Servers", "" + jda.getGuilds().size(), true);
        channel.sendMessageEmbeds(bd.build()).queue();
    }

}
