package com.naeayedea.keith.util;

import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class Database {

    private final Logger logger = LoggerFactory.getLogger(Database.class);

    private final DataSource dataSource;

    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //attempt to close an active connection
    private void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    //returns a prepared statement if the input string is valid
    public PreparedStatement prepareStatement(Connection connection, String string) {
        try {
            //noinspection SqlSourceToSinkFlow We are using prepared statements into this method anyway.
            return connection.prepareStatement(string);
        } catch (SQLException | NullPointerException e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    /* WARNING: This is very dangerous for the database as it does not use prepared statements so sql injection is
     * a very real possibility if you give users access to this function, should only be for *extremely* trusted
     * users.
     */
    public String executeQuery(String query) {
        try (Connection connection = getConnection()) {

            Statement statement = connection.createStatement();

            //noinspection SqlSourceToSinkFlow This is dangerous but we know this.
            ResultSet rs = statement.executeQuery(query);

            int columnCount = rs.getMetaData().getColumnCount();

            int length = 140;
            int minLength = 5;

            //This guarantees that columns will have a minimum length
            if ((length / minLength - 2) > columnCount) {
                length = length / columnCount;
            } else {
                length = minLength;
            }
            ArrayList<String> results = new ArrayList<>();

            StringBuilder currentResult = new StringBuilder();

            //Get column names
            for (int i = 1; i <= columnCount; i++) {
                currentResult.append(Utilities.truncateString(rs.getMetaData().getColumnLabel(i), length)).append("\t");
            }

            //get the information from each row separated by a tab
            results.add(currentResult.toString());
            while (rs.next()) {
                currentResult = new StringBuilder();
                //While next row
                for (int i = 1; i <= columnCount; i++) {
                    //Fill out each column
                    Object object = rs.getObject(i);
                    if (object != null) {
                        currentResult.append(Utilities.truncateString(String.valueOf(object), length)).append("\t");
                    } else {
                        currentResult.append(Utilities.truncateString("empty", length)).append("\t");
                    }
                }
                results.add(currentResult.toString());
            }
            rs.close();

            int n = 0;

            StringBuilder result = new StringBuilder("```");

            for (String resultSet : results) {
                if (n <= 20) {
                    result.append(resultSet).append("\n");
                }
                n++;
            }

            result.append("```");

            return result.toString();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();

        }
    }

    public List<String> getStringResult(String statementString, Object... args) {

        try (Connection connection = getConnection()) {
            if (statementString == null) {
                throw new SQLException("Statement is null");
            }

            //noinspection SqlSourceToSinkFlow we are using prepared statements here anyway
            return getStrings(executeStatement(connection.prepareStatement(statementString), args));
        } catch (SQLException | NullPointerException e) {
            logger.error(e.getMessage(), e);

            return new ArrayList<>(List.of("Error"));
        }
    }

    public EmbedBuilder getEmbedResult(PreparedStatement statement, Object... args) {
        try (Connection ignored = statement.getConnection()) {
            return getEmbed(executeStatement(statement, args));
        } catch (SQLException | NullPointerException e) {
            logger.error(e.getMessage(), e);

            EmbedBuilder error = new EmbedBuilder();
            error.setTitle("SQLException");
            error.setDescription(e.getMessage());

            return error;
        }
    }

    private void fillStatement(PreparedStatement statement, Object... args) throws SQLException, NullPointerException {
        int count = 0;
        for (Object object : args) {
            count++;
            //Determine type of object
            switch (object) {
                case String s -> statement.setString(count, s);
                case Integer i -> statement.setInt(count, i);
                case Long l -> statement.setLong(count, l);
                case Boolean b -> statement.setBoolean(count, b);
                case Double v -> statement.setDouble(count, v);
                case null, default ->
                    //warn me
                    logger.warn("Warning, class type {} not supported, details: {}", object != null ? object.getClass() : null, object);
            }
        }
    }

    public boolean executeUpdate(String statementString, Object... args) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = prepareStatement(connection, statementString);

            fillStatement(statement, args);

            statement.executeUpdate();
            return true;
        } catch (SQLException | NullPointerException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    //executes a premade prepared statement and takes in any parameters required
    private ResultSet executeStatement(PreparedStatement statement, Object... args) throws SQLException, NullPointerException {
        fillStatement(statement, args);
        return statement.executeQuery();
    }

    private ArrayList<String> getStrings(ResultSet rs) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        ArrayList<String> results = new ArrayList<>();
        StringBuilder currentResult = new StringBuilder();

        //Get column names
        for (int i = 1; i <= columnCount; i++) {
            currentResult.append(rs.getMetaData().getColumnName(i)).append("\t");
        }

        //get the information from each row separated by a tab
        results.add(currentResult.toString());
        while (rs.next()) {
            currentResult = new StringBuilder();
            //While next row
            for (int i = 1; i <= columnCount; i++) {
                //Fill out each column
                Object object = rs.getObject(i);
                if (object != null) {
                    currentResult.append(object).append("\t");
                } else {
                    currentResult.append("empty").append("\t");
                }
            }
            results.add(currentResult.toString());
        }
        rs.close();
        return results;
    }

    private EmbedBuilder getEmbed(ResultSet rs) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Search Result");
        /*
         * why tf embed fields cant be edited after creation
         */
        String[] headers = new String[columnCount];
        ArrayList<StringBuilder> columnContent = new ArrayList<>();

        //Get column headers and create stringbuilder for column content
        for (int i = 1; i <= columnCount; i++) {
            headers[i - 1] = rs.getMetaData().getColumnName(i);
            columnContent.add(new StringBuilder());
        }

        //retrieve column content
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                Object object = rs.getObject(i);
                if (object != null) {
                    if (columnContent.get(i - 1).length() + String.valueOf(object).length() <= 1024) {
                        columnContent.get(i - 1).append(object).append("\n");
                    }
                }
            }
        }

        //finally, build the embed
        for (int i = 0; i < columnCount; i++) {
            //only let three columns per line
            if (i > 0 && i % 2 == 0) {
                eb.addBlankField(false);
            }
            eb.addField(headers[i], columnContent.get(i).toString(), true);
        }
        rs.close();
        return eb;
    }

}
