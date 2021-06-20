package keith.util;

import keith.util.logs.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database {

    static Database instance;
    static DataSource source;


    public static void setSource(DataSource updatedSource){
        source = updatedSource;
    }

    //attempt connection to database
    private static Connection connect() throws SQLException {
            return source.getConnection();
    }

    //attempt to close an active connection
    private static void closeConnection(Connection connection) throws SQLException {
        if(connection != null) {
                connection.close();
        }
    }

    //returns a prepared statement if the input string is valid
    public static PreparedStatement prepareStatement(String string) {
        try {
            Connection connection = connect();
            return connection.prepareStatement(string);
        } catch (SQLException | NullPointerException e) {
            Logger.printWarning(e.getMessage());
            return null;
        }
    }

    public static ArrayList<String> getResultAsString(PreparedStatement statement, Object ... args) {
        try (Connection connection = statement.getConnection()) {
            return getStrings(executeStatement(statement, connection, args));
        } catch (SQLException e) {
            e.printStackTrace();
            ArrayList<String> error = new ArrayList<>();
            error.add("Error");
            return error;
        }
    }

    public static EmbedBuilder getResultAsEmbed(PreparedStatement statement, Object ... args) {
        try (Connection connection = statement.getConnection()) {
            return getEmbed(executeStatement(statement, connection, args));
        } catch (SQLException e) {
            e.printStackTrace();
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle("SQLException");
            error.setDescription(e.getMessage());
            return error;
        }
    }

    public static void executeStatementNoResult(PreparedStatement statement, Object ... args) {
        try (Connection connection = statement.getConnection()) {
            executeStatement(statement, connection, args);
        } catch (SQLException e) {
           Logger.printWarning("fuck");
        }
    }

    //executes a premade prepared statement and takes in any parameters required
    private static ResultSet executeStatement(PreparedStatement statement, Connection connection, Object ... args) throws SQLException{
        int count = 0;

            for (Object object : args) {
                count++;
                //Determine type of object
                if (object instanceof String) {
                    statement.setString(count, (String) object);
                } else if (object instanceof Integer) {
                    statement.setInt(count, (Integer) object);
                } else if (object instanceof Long) {
                    statement.setLong(count, (Long) object);
                } else if (object instanceof Boolean) {
                    statement.setBoolean(count, (Boolean) object);
                } else if (object instanceof Double) {
                    statement.setDouble(count, (Double) object);
                } else {
                    //warn me
                    Logger.printWarning("Warning, class type "+object.getClass()+" not supported");
                }
            }
        return statement.executeQuery();
    }

    private static ArrayList<String> getStrings(ResultSet rs) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        ArrayList<String> results = new ArrayList<>();
        StringBuilder currentResult= new StringBuilder();

        //Get column names
        for (int i = 1; i <= columnCount; i++) {
            currentResult.append(rs.getMetaData().getColumnName(i)).append("\t");
        }

        //get all of the information from each row separated by a tab
        results.add(currentResult.toString());
        while (rs.next()) {
            currentResult = new StringBuilder();
            //While next row
            for (int i = 1; i <= columnCount; i++) {
                //Fill out each column
                Object object = rs.getObject(i);
                if (object != null) {
                    currentResult.append(object.toString()).append("\t");
                }
            }
            results.add(currentResult.toString());
        }

        return results;
    }

    private static EmbedBuilder getEmbed(ResultSet rs) throws SQLException {
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
            headers[i-1] = rs.getMetaData().getColumnName(i);
            columnContent.add(new StringBuilder());
        }

        //retrieve column content
        while (rs.next()) {
            for(int i = 1; i <= columnCount; i++) {
                Object object = rs.getObject(i);
                if(object != null) {
                    columnContent.get(i-1).append(object.toString()).append("\n");
                }
            }
        }

        //finally, build the embed
        for (int i = 0; i < columnCount; i++) {
            //only let three columns per line
            if(i > 0 && i % 2 == 0) {
                eb.addBlankField(false);
            }
            eb.addField(headers[i], columnContent.get(i).toString(), true);
        }
        
        return eb;
    }

}
