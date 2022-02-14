package keith.util;

import keith.util.logs.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import org.sqlite.jdbc4.JDBC4Connection;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

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
        if (connection != null) {
            connection.close();
        }
    }

    //returns a prepared statement if the input string is valid
    public static PreparedStatement prepareStatement(String string) {
        try {
            return connect().prepareStatement(string);
        } catch (SQLException | NullPointerException e) {
            Logger.printWarning(e.getMessage());
            return null;
        }
    }

    /* WARNING: This is very dangerous for the database as it does not use prepared statements so sql injection is
     * a very real possibility if you give users access to this function, should only be for *extremely* trusted
     * users.
     */
    public static String executeQuery(String query) {
        try (Connection connection = connect() ) {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            int columnCount = rs.getMetaData().getColumnCount();
            int length = 140;
            int minLength = 5;
            //This guarantees that columns will have a minimum length
            if ((length / minLength - 2 ) > columnCount) {
                length = length / columnCount;
            } else {
                length = minLength;
            }
            ArrayList<String> results = new ArrayList<>();
            StringBuilder currentResult= new StringBuilder();

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
            StringBuilder result= new StringBuilder("```");
            for (String resultSet : results) {
                if (n <= 20) {
                    result.append(resultSet).append("\n");
                }
                n++;
            }
            result.append("```");
            return result.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static ArrayList<String> getStringResult(PreparedStatement statement, Object ... args) {
        try (Connection ignored = statement.getConnection()) {
            return getStrings(executeStatement(statement, args));
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            ArrayList<String> error = new ArrayList<>();
            error.add("Error");
            return error;
        }
    }

    public static EmbedBuilder getEmbedResult(PreparedStatement statement, Object ... args) {
        try (Connection ignored = statement.getConnection()) {
            return getEmbed(executeStatement(statement, args));
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle("SQLException");
            error.setDescription(e.getMessage());
            return error;
        }
    }

    private static void fillStatement(PreparedStatement statement, Object ... args) throws SQLException, NullPointerException{
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
                    Logger.printWarning("Warning, class type "+object.getClass()+" not supported, details: "+object);
                }
        }
    }

    public static boolean executeUpdate(PreparedStatement statement, Object ... args) {
        try (Connection ignored = statement.getConnection()) {
            fillStatement(statement, args);
            statement.executeUpdate();
            return true;
        } catch (SQLException | NullPointerException e) {
           Logger.printWarning(e.getMessage());
           return false;
        }
    }

    //executes a premade prepared statement and takes in any parameters required
    private static ResultSet executeStatement(PreparedStatement statement, Object ... args) throws SQLException, NullPointerException{
        fillStatement(statement, args);
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
                    if (columnContent.get(i-1).length() + String.valueOf(object).length() <= 1024) {
                        columnContent.get(i-1).append(object).append("\n");
                    }
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
        rs.close();
        return eb;
    }

}
