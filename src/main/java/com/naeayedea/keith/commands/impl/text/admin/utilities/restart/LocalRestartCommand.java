package com.naeayedea.keith.commands.impl.text.admin.utilities.restart;

import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnMissingBean(BaseRestartCommand.class)
public class LocalRestartCommand extends BaseRestartCommand {

    private static final Logger logger = LoggerFactory.getLogger(LocalRestartCommand.class);

    private final String version;

    private final String projectName;

    public LocalRestartCommand(@Value("${keith.commands.admin.utilities.restart.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.restart.aliases}', ',')}") List<String> commandAliases, @Value("${keith.version}") String version, @Value("${keith.project.name}") String projectName) {
        super(defaultName, commandAliases);
        this.version = version;
        this.projectName = projectName;
    }

    @PostConstruct
    public void init() {
        logger.info("Restart configured to use jar command: {}", (Object) getRestartArguments("[Placeholder Message Id]", "[Placeholder Channel Id]"));
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        try {
            Message message = event.getChannel().sendMessage("Restarting...").complete();

            Process p = Runtime.getRuntime().exec(getRestartArguments(message.getId(), message.getChannelId()));

            Utilities.setStatus("Restarting...");

            try {
                if (p.waitFor(10, TimeUnit.SECONDS)) {
                    //restart with a non-zero exit code so the system service restarts us automatically
                    System.exit(1);
                } else {
                    event.getChannel().sendMessage("Restart failed...").queue();
                }
            } catch (InterruptedException ignored) {}

        } catch (IOException e) {
            event.getChannel().sendMessage("Restart failed badly...").queue();
        }
    }

    private String[] getRestartArguments(String messageId, String channelId) {
        return new String[]{"java", "-jar", System.getProperty("user.dir") + "\\" + projectName + "-" + version +".jar", messageId, channelId};
    }
}
