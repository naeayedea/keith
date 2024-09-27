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
        logger.info("Starting program, received args {}", (Object[]) args.getSourceArgs());
    }
}
