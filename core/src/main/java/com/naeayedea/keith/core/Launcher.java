package com.naeayedea.keith.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.naeayedea")
public class Launcher implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SpringApplication.run(Launcher.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.getSourceArgs() != null && args.getSourceArgs().length != 0) {
            logger.info("Keith Core initialized, received args {}", (Object[]) args.getSourceArgs());
        } else {
            logger.info("Keith Core initialized");
        }
    }
}
