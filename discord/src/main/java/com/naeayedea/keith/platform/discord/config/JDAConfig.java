package com.naeayedea.keith.platform.discord.config;

import com.naeayedea.keith.platform.discord.utils.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JDAConfig {

    @Value("${keith.configuration.token}")
    private String token;

    private final Logger logger = LoggerFactory.getLogger(JDAConfig.class);

    @Bean
    public DiscordConfiguration keith(ApplicationArguments arguments) {
        String[] args = arguments.getSourceArgs();

        logger.info("Loading bot configuration...");

        return new DiscordConfiguration(
            token,
            args.length > 2 ? args[0] : "",
            args.length > 2 ? args[1] : ""
        );
    }

    @Bean
    public JDA jda(DiscordConfiguration discordConfiguration, List<CommandData> commands) throws Exception {
        logger.info("Initialising JPA");

        JDABuilder builder = JDABuilder.create(discordConfiguration.getToken(),
            GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EXPRESSIONS,
            GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.MESSAGE_CONTENT
        );

        try {
            JDA jda = builder
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableCache(CacheFlag.SCHEDULED_EVENTS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setLargeThreshold(50)
                .build()
                .awaitReady();

            logger.info("Adding {} slash commands.", commands.size());

            jda.updateCommands()
                .addCommands(commands)
                .queue(s -> {
                    logger.info("Loaded {} commands.", s.size());
                    logger.info("Commands loaded: {}", s);
                });

            logger.info("JDA ready. Preparing pagination.");

            if (!discordConfiguration.getRestartMessage().isEmpty()) {
                logger.info("Restart message \"{}\" received for channel \"{}\"", discordConfiguration.getRestartMessage(), discordConfiguration.getRestartChannel());

                TextChannel channel = jda.getTextChannelById(discordConfiguration.getRestartChannel());
                if (channel != null) {
                    channel.retrieveMessageById(discordConfiguration.getRestartMessage()).queue(message -> message.editMessage("Restarted").queue());
                }
            }

            Utilities.setJDA(jda);

            Utilities.forceDefaultStatus();

            return jda;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

}
