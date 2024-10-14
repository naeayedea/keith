package com.naeayedea.keith.commands.text.admin.utilities;

import com.naeayedea.keith.util.Utilities;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RestartTextCommand extends AbstractAdminUtilsTextCommand {

    private static final Logger logger = LoggerFactory.getLogger(RestartTextCommand.class);

    @Value("${keith.version}")
    private String version;

    @Value("${keith.project.name}")
    private String projectName;


    public RestartTextCommand(@Value("${keith.commands.admin.utilities.restart.defaultName}") String defaultName, @Value("#{T(com.naeayedea.keith.converter.StringToAliasListConverter).convert('${keith.commands.admin.utilities.restart.aliases}', ',')}") List<String> commandAliases) {
        super(defaultName, commandAliases);
    }

    @PostConstruct
    public void init() {
        logger.info("Restart configured to use jar command: {}", (Object) getRestartArguments("[Placeholder Message Id]", "[Placeholder Channel Id]"));
    }

    @Override
    public String getExampleUsage(String prefix) {
        return prefix + getDefaultName() + ": \"restarts the bot\"";
    }

    @Override
    public String getDescription() {
        return "Will shutdown and relaunch the bots processes - useful after update";
    }

    @Override
    public void run(MessageReceivedEvent event, List<String> tokens) {
        try {
            Message message = event.getChannel().sendMessage("Restarting...").complete();

            Process p = Runtime.getRuntime().exec(getRestartArguments(message.getId(), message.getChannelId()));

            Utilities.setStatus("Restarting...");

            try {
                if (p.waitFor(10, TimeUnit.SECONDS)) {
                    Utilities.runShutdownProcedure();
                } else {
                    event.getChannel().sendMessage("Restart failed...").queue();
                }
            } catch (InterruptedException ignored) {}

        } catch (IOException e) {
            event.getChannel().sendMessage("Restart failed badly...").queue();
        }
    }

    private String[] getRestartArguments(String messageId, String channelId) {
        return new String[]{"screen", "-dm", "java", "-jar", System.getProperty("user.dir") + "\\" + projectName + "-" + version +".jar", messageId, channelId};
    }
}
