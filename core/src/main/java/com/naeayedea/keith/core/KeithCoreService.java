/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.naeayedea")
public class KeithCoreService implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(KeithCoreService.class);

    public static void main(String[] args) {
        SpringApplication.run(KeithCoreService.class, args);
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
