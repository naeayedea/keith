package com.naeayedea.config.manager;

import com.naeayedea.keith.managers.ChannelCommandManager;
import com.naeayedea.keith.managers.ServerChatManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.CandidateManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ManagerConfig {

    @Bean
    public ServerManager serverManager() {
        return new ServerManager();
    }

    @Bean
    public CandidateManager candidateManager() {
        return new CandidateManager();
    }

    @Bean
    public ChannelCommandManager channelCommandManager() {
        return new ChannelCommandManager();
    }

    @Bean
    public ServerChatManager serverChatManager(ServerManager serverManager) {
        return new ServerChatManager(serverManager);
    }

}
