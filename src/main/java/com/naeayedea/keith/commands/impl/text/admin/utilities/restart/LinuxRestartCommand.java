package com.naeayedea.keith.commands.impl.text.admin.utilities.restart;

import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("remote")
public class LinuxRestartCommand extends BaseRestartCommand {

    private static final Logger logger = LoggerFactory.getLogger(LinuxRestartCommand.class);

    private final ApplicationContext applicationContext;

    public LinuxRestartCommand(@Value("${keith.commands.admin.utilities.restart.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.restart.aliases}', ',')}") List<String> commandAliases, ApplicationContext applicationContext) {
        super(defaultName, commandAliases);

        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        logger.info("Restart configured to rely on systemd restart mechanism");
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        event.getChannel().sendMessage("Restarting...").queue();

        Utilities.setStatus("Restarting...");

        //restart with a non-zero exit code so the system service restarts us automatically
        System.exit(1);
    }

}
