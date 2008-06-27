/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

import de.tor.tribes.sec.SecurityAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.omg.CORBA.INITIALIZE;

/**
 *
 * @author Jejkal
 */
public class DatabaseAdapter {

    public static final int ID_UNKNOWN_ERROR = -1;
    public static final int ID_SUCCESS = 0;
    public static final int ID_CONNECTION_FAILED = 1;
    public static final int ID_USER_ALREADY_EXIST = 2;
    public static final int ID_USER_NOT_EXIST = 3;
    private static Connection DB_CONNECTION = null;
    private static Logger logger = Logger.getLogger(DatabaseAdapter.class);
    private static boolean DRIVER_AVAILABLE = false;
    private static boolean INITIALIZED = false;

    private static void findDriver() {
        //try to load the MySQL driver
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            logger.info("Database driver loaded");
            DRIVER_AVAILABLE = true;
        } catch (Exception ex) {
            logger.error("Database driver not found");
            DRIVER_AVAILABLE = false;
        }
        INITIALIZED = true;
    }

    /**Open the database connection*/
    private static boolean openConnection() {
        if (!INITIALIZED) {
            findDriver();
        }
        if (!DRIVER_AVAILABLE) {
            logger.warn("Not connecting, driver not available");
            return false;
        }
        try {
            DB_CONNECTION = DriverManager.getConnection("jdbc:mysql://www.torridity.de/dsworkbench?" + "user=dsworkbench&password=DSwb'08");
            return true;
        } catch (SQLException se) {
            logger.error("Failed to establish database connection", se);
            return false;
        }
    }

    /**Close the database connection*/
    private static void closeConnection() {
        try {
            DB_CONNECTION.close();
        } catch (Throwable t) {
        }
    }

    /**Check if a user is in the database and if the provided password is correct
     * @param pUsername Name of the user to check
     * @param pPassword Password of the user to check
     * @return int Success=0
     */
    public static int checkUser(String pUsername, String pPassword) {
        if (!openConnection()) {
            System.out.println("Datenbankverbindung fehlgeschlagen");
            return ID_CONNECTION_FAILED;
        }

        int retVal = ID_SUCCESS;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT COUNT(*) FROM users WHERE name='" + pUsername + "' AND password='" + SecurityAdapter.hashStringMD5(pPassword) + "';";
            ResultSet rs = s.executeQuery(query);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (count != 1) {
                if (count == 0) {
                    retVal = ID_USER_NOT_EXIST;
                } else {
                    throw new Exception("There are " + count + " users with name '" + pUsername + "' inside the database");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to validate user", e);
            retVal = ID_UNKNOWN_ERROR;
        }
        closeConnection();
        return retVal;
    }

    /**Add a user to the database
     * @param pUsername Name of the user to add
     * @param pPassword Password of the user
     * @return int Success=0
     */
    public static int addUser(String pUsername, String pPassword) {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }
        int retVal = ID_SUCCESS;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT COUNT(*) FROM users WHERE name='" + pUsername + "';";
            ResultSet rs = s.executeQuery(query);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (count != 0) {
                retVal = ID_USER_ALREADY_EXIST;
            } else {
                String update = "INSERT INTO users(name, password) VALUES ('" + pUsername + "', '" + SecurityAdapter.hashStringMD5(pPassword) + "');";
                if (s.executeUpdate(update) != 1) {
                    throw new Exception("Unknown error while adding user to database");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to add user", e);
            retVal = ID_UNKNOWN_ERROR;
        }
        closeConnection();
        return retVal;
    }

    /**Check when the last update of server data was made
     * @param pUsername User who want to perform the update
     * @param pServer Server on which the update should be performed
     * @return long Timestamp of the last update
     */
    public static long checkLastUpdate(String pUsername, String pServer) {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }
        int retVal = ID_SUCCESS;
        long lastUpdate = 0;
        try {
            //get user id
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT id FROM users WHERE name='" + pUsername + "';";
            ResultSet rs = s.executeQuery(query);
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            if (id == -1) {
                throw new Exception("ID for user " + pUsername + " not found");
            }
            //check last update
            s = DB_CONNECTION.createStatement();

            query = "SELECT timestamp FROM updates WHERE UserID=" + id + " AND ServerID='" + pServer + "';";
            rs = s.executeQuery(query);

            while (rs.next()) {
                lastUpdate = rs.getTimestamp(1).getTime();
            }
            if (lastUpdate == 0) {
                logger.info("No update made yet.");
            }
        } catch (Exception e) {
            logger.error("Failed to check for last update", e);
            retVal = ID_UNKNOWN_ERROR;
        }
        closeConnection();
        return lastUpdate;
    }

    /**Store an update of server data in the database
     * @param pUsername User who performed the update
     * @param pServer Server on which the update was performed
     * @return int Success=0
     */
    public static int storeLastUpdate(String pUsername, String pServer) {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }
        int retVal = ID_SUCCESS;

        try {
            //get user id
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT id FROM users WHERE name='" + pUsername + "';";
            ResultSet rs = s.executeQuery(query);
            int id = -1;
            while (rs.next()) {
                id = rs.getInt(1);
            }
            if (id == -1) {
                throw new Exception("ID for user " + pUsername + " not found");
            }
            //check last update
            s = DB_CONNECTION.createStatement();
            query = "DELETE FROM updates WHERE UserID=" + id + " AND ServerID='" + pServer + "';";
            int changed = s.executeUpdate(query);

            s = DB_CONNECTION.createStatement();
            query = "INSERT INTO updates(UserID, ServerID, timestamp) VALUES(" + id + ",'" + pServer + "', CURRENT_TIMESTAMP);";
            changed = s.executeUpdate(query);

            if (changed != 1) {
                throw new Exception("Failed to update timestamp");
            }
        } catch (Exception e) {
            logger.error("Failed to check for last update", e);
            retVal = ID_UNKNOWN_ERROR;
        }
        closeConnection();
        return retVal;
    }

    /**Returns the current server time in milliseconds
     * @return long The current server time
     */
    public static long getCurrentServerTime() {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }

        long t = 0;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT NOW();";
            ResultSet rs = s.executeQuery(query);
            while (rs.next()) {
                t = rs.getTimestamp(1).getTime();
            }
        } catch (Exception e) {
            logger.error("Failed to obtain the current server time", e);
        }
        closeConnection();
        return t;
    }

    public static void main(String[] args) {
        DOMConfigurator.configure("log4j.xml");
        /*  System.out.println(DatabaseAdapter.addUser("Torridity", "realstyx13"));
        long s = System.currentTimeMillis();
        System.out.println(DatabaseAdapter.checkUser("Torridity", "realstyx13"));*/

        System.out.println(DatabaseAdapter.getCurrentServerTime());
    /*System.out.println("Check: " + DatabaseAdapter.checkLastUpdate("Torridity", "de26"));
    System.out.println("Store: " + DatabaseAdapter.storeLastUpdate("Torridity", "de26"));
    System.out.println("Check: " + DatabaseAdapter.checkLastUpdate("Torridity", "de26"));*/

    // System.out.println("Du " + (System.currentTimeMillis() - s));
    }
}
