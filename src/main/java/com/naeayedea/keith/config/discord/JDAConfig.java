package com.naeayedea.keith.config.discord;

import com.github.ygimenez.model.PaginatorBuilder;
import com.naeayedea.keith.model.BotConfiguration;
import com.naeayedea.keith.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class JDAConfig {

    @Value("${keith.configuration.token}")
    private String token;

    private final Logger logger = LoggerFactory.getLogger(JDAConfig.class);

    @Bean
    public BotConfiguration keith(DataSource dataSource, ApplicationArguments arguments) {
        String[] args = arguments.getSourceArgs();

        logger.info("Loading bot configuration...");

        return new BotConfiguration(
            token,
            dataSource
            ,
            args.length > 2 ? args[0] : "",
            args.length > 2 ? args[1] : ""
        );
    }

    @Bean
    public JDA jda(BotConfiguration botConfiguration, List<CommandData> commands) throws Exception {
        logger.info("Initialising JPA");

        JDABuilder builder = JDABuilder.create(botConfiguration.getToken(),
            GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.MESSAGE_CONTENT
        );

        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setLargeThreshold(50);

        try {
            JDA jda = builder
                .build()
                .awaitReady();

            logger.info("Adding commands. Count {}", commands.size());

            jda.updateCommands()
                .addCommands(commands)
                .queue();

            logger.info("JDA ready. Preparing pagination.");

            PaginatorBuilder.createPaginator(jda)
                .shouldRemoveOnReact(false)
                .shouldEventLock(true)
                .setDeleteOnCancel(true)
                .activate();

            if (!botConfiguration.getRestartMessage().isEmpty()) {
                logger.info("Restart message \"{}\" received for channel \"{}\"", botConfiguration.getRestartMessage(), botConfiguration.getRestartChannel());

                TextChannel channel = jda.getTextChannelById(botConfiguration.getRestartChannel());
                if (channel != null) {
                    channel.retrieveMessageById(botConfiguration.getRestartMessage()).queue(message -> message.editMessage("Restarted").queue());
                }
            }

            Utilities.setJDA(jda);

            jda.getPresence().setActivity(Activity.playing("?help for commands | " + jda.getGuilds().size() + " servers"));

            return jda;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

}
