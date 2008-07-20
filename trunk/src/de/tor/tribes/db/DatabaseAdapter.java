/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

import de.tor.tribes.sec.SecurityAdapter;
import de.tor.tribes.util.GlobalOptions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author Jejkal
 */
public class DatabaseAdapter {

    private static Logger logger = Logger.getLogger(DatabaseAdapter.class);
    public static final int ID_UNKNOWN_ERROR = -4711;
    public static final int ID_SUCCESS = 0;
    public static final int ID_CONNECTION_FAILED = -1;
    public static final int ID_USER_ALREADY_EXIST = -2;
    public static final int ID_DUAL_ACCOUNT = -3;
    public static final int ID_USER_NOT_EXIST = -4;
    public static final int ID_WRONG_PASSWORD = -5;
    public static final int ID_VERSION_NOT_ALLOWED = -6;
    public static final long ID_UPDATE_NEVER = -666;
    private static Connection DB_CONNECTION = null;
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
                    query = "SELECT COUNT(*) FROM users WHERE name='" + pUsername + "';";
                    rs = s.executeQuery(query);
                    while (rs.next()) {
                        count = rs.getInt(1);
                    }
                    if (count == 0) {
                        retVal = ID_USER_NOT_EXIST;
                    } else {
                        retVal = ID_WRONG_PASSWORD;
                    }
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
            String query = "SELECT COUNT(*) FROM registrants WHERE uniqueKey='" + SecurityAdapter.getUniqueID() + "';";
            ResultSet rs = s.executeQuery(query);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (count != 0) {
                logger.error("User obviously already created an account");
                retVal = ID_DUAL_ACCOUNT;
            }

            if (retVal == ID_SUCCESS) {
                s = DB_CONNECTION.createStatement();
                query = "SELECT COUNT(*) FROM users WHERE name='" + pUsername + "';";
                rs = s.executeQuery(query);
                count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }

                if (count != 0) {
                    retVal = ID_USER_ALREADY_EXIST;
                } else {
                    s = DB_CONNECTION.createStatement();
                    String update = "INSERT INTO users(name, password) VALUES ('" + pUsername + "', '" + SecurityAdapter.hashStringMD5(pPassword) + "');";
                    if (s.executeUpdate(update) != 1) {
                        throw new Exception("Unknown error while adding user to database");
                    }

                    //get user id
                    s = DB_CONNECTION.createStatement();
                    query = "SELECT id FROM users WHERE name='" + pUsername + "';";
                    rs = s.executeQuery(query);
                    int id = -1;
                    while (rs.next()) {
                        id = rs.getInt(1);
                    }
                    if (id == -1) {
                        logger.error("Failed to get user id");
                    } else {
                        s = DB_CONNECTION.createStatement();
                        update = "INSERT INTO registrants(uniqueKey, userID) VALUES ('" + SecurityAdapter.getUniqueID() + "'," + id + ")";
                        if (s.executeUpdate(update) != 1) {
                            logger.error("Failed to store unique ID in database");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to add user", e);
            e.printStackTrace();
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
    public static boolean isUpdatePossible(String pUsername, String pServer) {
        if (!openConnection()) {
            return false;
        }

        boolean retVal = false;
        long lastUpdate = ID_UPDATE_NEVER;
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

            if (lastUpdate == ID_UPDATE_NEVER) {
                logger.info("No update made yet.");
                retVal = true;
            } else {
                long currentTime = getCurrentServerTime();
                if ((currentTime < 0) || (lastUpdate < 0)) {
                    //failed to get last update or current time
                    return false;
                }

                //get delte between now and the last update
                long delta = currentTime - lastUpdate;

                //s = DB_CONNECTION.createStatement();
                query = "SELECT value FROM settings WHERE variable='trime_between_update';";
                rs = s.executeQuery(query);
                String value = null;
                while (rs.next()) {
                    value = rs.getString("value");
                }

                long minDelta = -1;
                if (value != null) {
                    try {
                        minDelta = Long.parseLong(value);
                    } catch (Exception e) {
                        minDelta = ID_UNKNOWN_ERROR;
                    }
                } else {
                    minDelta = ID_UNKNOWN_ERROR;
                }

                if (minDelta < 0) {
                    logger.error("Failed to check min update interval");
                    retVal = false;
                }

                if (delta < minDelta) {
                    //more than one update per day not allowed
                    retVal = false;
                } else {
                    retVal = true;
                }

            }
        } catch (Exception e) {
            logger.error("Failed to check for last update", e);
            retVal = false;
        }

        closeConnection();
        return retVal;
    }

    public static long getTimeSinceLastUpdate(String pUsername, String pServer) {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }

        long delta = 0;
        long lastUpdate = ID_UPDATE_NEVER;
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
           // s = DB_CONNECTION.createStatement();

            query = "SELECT timestamp FROM updates WHERE UserID=" + id + " AND ServerID='" + pServer + "';";
            rs = s.executeQuery(query);

            while (rs.next()) {
                lastUpdate = rs.getTimestamp(1).getTime();
            }
            //get current server time
            long currentServerTime = getCurrentServerTime();

            //last update is only set UPDATE_NEVER if no update was made yet
            if (lastUpdate == ID_UPDATE_NEVER) {
                logger.info("No update made yet.");
                delta =
                        currentServerTime;
            } else {
                if (currentServerTime < 0) {
                    //failed to get last update or current time
                    logger.error("Failed to read server time");
                    delta =
                            0;
                } else {
                    //get delta between now and the last update
                    delta = currentServerTime - lastUpdate;
                }

            }
        } catch (Exception e) {
            logger.error("Failed to check for last update", e);
            delta =
                    0;
        }

        closeConnection();
        return delta;
    }

    public static int isVersionAllowed() {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }

        int retVal = ID_SUCCESS;
        try {
            //get user id
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT value FROM settings WHERE variable='min_version';";
            ResultSet rs = s.executeQuery(query);
            String min_version = null;
            while (rs.next()) {
                min_version = rs.getString("value");
            }

            if (min_version != null) {
                try {
                    double v = Double.parseDouble(min_version);
                    if (GlobalOptions.VERSION < v) {
                        retVal = ID_VERSION_NOT_ALLOWED;
                    }

                } catch (Exception e) {
                    retVal = ID_UNKNOWN_ERROR;
                }

            } else {
                retVal = ID_UNKNOWN_ERROR;
            }

        } catch (Exception e) {
            logger.error("Failed to check min version", e);
            retVal =
                    ID_UNKNOWN_ERROR;
        }

        closeConnection();
        return retVal;
    }

    public static long getMinUpdateInterval() {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }

        long retVal = ID_SUCCESS;
        try {
            //get user id
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT value FROM settings WHERE variable='trime_between_update';";
            ResultSet rs = s.executeQuery(query);
            String value = null;
            while (rs.next()) {
                value = rs.getString("value");
            }

            if (value != null) {
                try {
                    retVal = Long.parseLong(value);
                } catch (Exception e) {
                    retVal = ID_UNKNOWN_ERROR;
                }

            } else {
                retVal = ID_UNKNOWN_ERROR;
            }

        } catch (Exception e) {
            logger.error("Failed to check min version", e);
            retVal =
                    ID_UNKNOWN_ERROR;
        }

        closeConnection();
        return retVal;
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
            //s = DB_CONNECTION.createStatement();
            query = "DELETE FROM updates WHERE UserID=" + id + " AND ServerID='" + pServer + "';";
            int changed = s.executeUpdate(query);

            //s = DB_CONNECTION.createStatement();
            query = "INSERT INTO updates(UserID, ServerID, timestamp) VALUES(" + id + ",'" + pServer + "', CURRENT_TIMESTAMP);";
            changed = s.executeUpdate(query);

            if (changed != 1) {
                throw new Exception("Failed to update timestamp");
            }

        } catch (Exception e) {
            logger.error("Failed to check for last update", e);
            retVal =
                    ID_UNKNOWN_ERROR;
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
        System.setProperty("proxyUse", "true");
        System.setProperty("proxyHost", "proxy.fzk.de");
        System.setProperty("proxyPort", "8000");
        System.out.println(DatabaseAdapter.checkUser("Torridity", "realstyx13"));
    /*  System.out.println(DatabaseAdapter.addUser("Torridity", "realstyx13"));
    long s = System.currentTimeMillis();
    System.out.println(DatabaseAdapter.checkUser("Torridity", "realstyx13"));*/

    //  System.out.println(DatabaseAdapter.isUpdatePossible("Torridity", "de3"));
    /*System.out.println("Check: " + DatabaseAdapter.checkLastUpdate("Torridity", "de26"));
    System.out.println("Store: " + DatabaseAdapter.storeLastUpdate("Torridity", "de26"));
    System.out.println("Check: " + DatabaseAdapter.checkLastUpdate("Torridity", "de26"));*/

    // System.out.println("Du " + (System.currentTimeMillis() - s));

    }
}
