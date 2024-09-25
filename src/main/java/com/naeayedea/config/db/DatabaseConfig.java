package com.naeayedea.config.db;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig {

    @Value("${keith.database.url}")
    private String keithDatabaseUrl;

    @Value("${keith.database.user}")
    private String keithDatabaseUser;

    @Value("${keith.database.password}")
    private String keithDatabasePassword;

    @Bean
    public DataSource keithDataSource() throws SQLException {
        MariaDbDataSource dataSource = new MariaDbDataSource();

        dataSource.setUrl(keithDatabaseUrl);
        dataSource.setUser(keithDatabaseUser);
        dataSource.setPassword(keithDatabasePassword);

        return dataSource;
    }
}
