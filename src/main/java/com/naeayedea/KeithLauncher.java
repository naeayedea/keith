package com.naeayedea;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;


@SpringBootApplication
public class KeithLauncher implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(KeithLauncher.class);

    public static void main(String[] args) {
        SpringApplication.run(KeithLauncher.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting program, received args {}", (Object[]) args.getSourceArgs());
    }
}
