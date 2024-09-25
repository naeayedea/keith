package com.naeayedea.config.manager;

import com.naeayedea.keith.managers.ChannelCommandManager;
import com.naeayedea.keith.managers.ServerChatManager;
import com.naeayedea.keith.managers.ServerManager;
import com.naeayedea.keith.managers.UserManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ManagerConfig {


    @Bean
    public ServerManager serverManager() {
        return ServerManager.getInstance();
    }

    @Bean
    public UserManager userManager() {
        return UserManager.getInstance();
    }

    @Bean
    public ChannelCommandManager channelCommandManager() {
        return ChannelCommandManager.getInstance();
    }

    @Bean
    public ServerChatManager serverChatManager() {
        return ServerChatManager.getInstance();
    }


}
