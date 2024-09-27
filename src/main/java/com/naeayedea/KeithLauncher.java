package com.naeayedea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class KeithLauncher implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(KeithLauncher.class);

    public static void main(String[] args) {
        SpringApplication.run(KeithLauncher.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.getSourceArgs() != null && args.getSourceArgs().length != 0) {
            logger.info("Keith initialized, received args {}", (Object[]) args.getSourceArgs());
        } else {
            logger.info("Keith initialized");
        }
    }
}
