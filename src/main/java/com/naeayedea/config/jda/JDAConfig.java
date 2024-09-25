package com.naeayedea.config.jda;

import com.github.ygimenez.model.PaginatorBuilder;
import com.naeayedea.model.BotConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

@Configuration
public class JDAConfig {

    @Value("${keith.configuration.token}")
    private String token;

    Logger logger = LoggerFactory.getLogger(JDAConfig.class);

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
    public JDA jda(BotConfiguration botConfiguration) throws Exception {
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

            logger.info("JPA ready. Preparing pagination.");

            //configure pagination library
            PaginatorBuilder.createPaginator()
                .setHandler(jda)
                .shouldRemoveOnReact(false)
                .shouldEventLock(true)
                .activate();

            PaginatorBuilder.createPaginator(jda)
                .shouldRemoveOnReact(false)
                .shouldEventLock(true)
                .setDeleteOnCancel(true)
                .build();

            if(!botConfiguration.getRestartMessage().isEmpty()) {
                logger.info("Restart message \"{}\" received for channel \"{}\"", botConfiguration.getRestartMessage(), botConfiguration.getRestartChannel());

                TextChannel channel = jda.getTextChannelById(botConfiguration.getRestartChannel());
                if (channel != null) {
                    channel.retrieveMessageById( botConfiguration.getRestartMessage()).queue(message -> message.editMessage("Restarted").queue());
                }
            }

            return jda;
        } catch (Throwable e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

}
