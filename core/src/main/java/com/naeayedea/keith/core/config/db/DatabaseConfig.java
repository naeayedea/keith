/*
 * Copyright (C) Steven Muirhead 2025. All Rights Reserved.
 *
 * Unauthorized copying, or use of the contents of this file via any medium is
 * strictly prohibited unless previous permission has been given by the
 * copyright holder(s) in writing.
 *
 */

package com.naeayedea.keith.core.config.db;

import org.mariadb.jdbc.MariaDbDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${keith.database.schema.minimum-version}")
    private String minimumSchemaVersion;

    @Value("${keith.database.schema.maximum-version:${keith.database.schema.minimum-version}}")
    private String maximumSchemaVersion;

    @Bean
    @ConfigurationProperties(prefix = "keith.database")
    public DatabaseProperties keithDatabaseProperties() {
        return new DatabaseProperties();
    }

    @Bean
    public DataSource keithDataSource(DatabaseProperties keithDatabaseProperties) throws SQLException {
        MariaDbDataSource dataSource = new MariaDbDataSource();

        dataSource.setUrl(keithDatabaseProperties.getUrl());
        dataSource.setUser(keithDatabaseProperties.getUser());
        dataSource.setPassword(keithDatabaseProperties.getPassword());

        //check that connection works
        Connection connection = dataSource.getConnection();

        connection.prepareStatement("SELECT 1;").executeQuery();

        connection.close();

        return dataSource;
    }

    @Bean
    public JdbcClient keithJdbcClient(DataSource keithDataSource) {
        JdbcClient client = JdbcClient.create(keithDataSource);

        validateSchemaVersion(client);

        return client;
    }

    private void validateSchemaVersion(JdbcClient keithJdbcClient) {
        List<Map<String, Object>> resultSet = keithJdbcClient.sql("SELECT * FROM schema_history ORDER BY date_updated DESC LIMIT 1")
            .query()
            .listOfRows();

        if (resultSet.size() != 1) throw new AssertionError("Expected a single schema history version to be loaded");

        Map<String, Object> schemaHistory = resultSet.getFirst();

        String majorVersionString = schemaHistory.getOrDefault("major_version", "").toString();
        String minorVersionString = schemaHistory.getOrDefault("minor_version", "").toString();
        String patchVersionString = schemaHistory.getOrDefault("patch_version", "").toString();
        String versionMetadata = schemaHistory.getOrDefault("version_metadata", "").toString();

        if (!versionMetadata.isBlank()) {
            versionMetadata = "-" +  versionMetadata;
        }

        int majorVersion = parsePositiveInt(majorVersionString);
        int minorVersion = parsePositiveInt(minorVersionString);
        int patchVersion = parsePositiveInt(patchVersionString);

        String completeVersionString = majorVersion + "." + minorVersion + "." + patchVersion + versionMetadata;

        List<Integer> minimumSchemaValues = loadVersionValues(minimumSchemaVersion);
        List<Integer> maximumSchemaValues = loadVersionValues(maximumSchemaVersion);

        for (int i = 0; i < minimumSchemaValues.size() && i < maximumSchemaValues.size(); i++) {
            if (minimumSchemaValues.get(i) > maximumSchemaValues.get(i)) {
                throw new IllegalArgumentException("Minimum version number must be less than the maximum");
            }
        }

        if (majorVersion < minimumSchemaValues.getFirst() || majorVersion > maximumSchemaValues.getFirst()) {
            throw new IllegalStateException("Major schema version of database is not compatible (" + minimumSchemaVersion + " ~ "  + maximumSchemaVersion + " vs " + completeVersionString + ")");
        }

        if (minorVersion < minimumSchemaValues.get(1) || minorVersion > maximumSchemaValues.get(1)) {
            throw new IllegalStateException("Minor schema version of database is not compatible (" + minimumSchemaVersion + " ~ "  + maximumSchemaVersion + " vs " + completeVersionString + ")");
        }

        logger.info("Loaded database schema {}", completeVersionString);
    }

    private List<Integer> loadVersionValues(String versionNumber) {
        List<Integer> schemaValues = Arrays.stream(maximumSchemaVersion.split("\\.")).map(this::parseInt).toList();

        if (schemaValues.size() < 2)
            throw new AssertionError("Expect at least MAJOR.MINOR version scheme for version number:  " + versionNumber);

        return schemaValues;
    }

    private int parsePositiveInt(@NonNull String value) {
        int parsedInteger = parseInt(value);

        if (parsedInteger < 0) {
            throw new IllegalArgumentException(value + " is not a positive integer");
        }

        return parsedInteger;
    }

    private int parseInt(@NonNull String value) {
        try {
            return Integer.parseInt(value);
        }  catch (NumberFormatException e) {
            throw new IllegalArgumentException(value + " is not an integer");
        }
    }
}
