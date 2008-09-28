/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

import de.tor.tribes.sec.SecurityAdapter;
import de.tor.tribes.util.Constants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
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
    public static final int ID_UPDATE_NEVER = -666;
    public static final int ID_UPDATE_NOT_ALLOWED = -666;
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
            logger.error("Database driver not found", ex);
            DRIVER_AVAILABLE = false;
        }
        INITIALIZED = true;
    }
    private static boolean localMode = false;

    public static void setToLocalMode() {
        localMode = true;
    }

    public static boolean isLocalMode() {
        return localMode;
    }

    /**Open the database connection*/
    private static boolean openConnection() {
        if (isLocalMode()) {
            return openLocalConnection();
        }
        if (!INITIALIZED) {
            findDriver();
        }
        if (!DRIVER_AVAILABLE) {
            logger.warn("Not connecting, driver not available");
            return false;
        }
        try {
            DB_CONNECTION = DriverManager.getConnection("jdbc:mysql://www.dsworkbench.de/dsworkbench?" + "user=dsworkbench&password=DSwb'08");
            return true;
        } catch (SQLException se) {
            logger.error("Failed to establish database connection", se);
            return false;
        }
    }

    /**Open the database connection*/
    private static boolean openLocalConnection() {
        if (!INITIALIZED) {
            findDriver();
        }
        if (!DRIVER_AVAILABLE) {
            logger.warn("Not connecting, driver not available");
            return false;
        }
        try {
            DB_CONNECTION = DriverManager.getConnection("jdbc:mysql://localhost/dsworkbench?" + "user=dsworkbench&password=DSwb'08");
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
            String query = "SELECT COUNT(*) FROM users WHERE name='" + pUsername + "';";
            ResultSet rs = s.executeQuery(query);
            int count = 0;
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
            }
        } catch (Exception e) {
            logger.error("Failed to add user", e);
            e.printStackTrace();
            retVal = ID_UNKNOWN_ERROR;
        }

        closeConnection();
        return retVal;
    }

    public static String getPropertyValue(String pKey) {
        if (!openConnection()) {
            return null;
        }
        String retVal = null;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT value FROM settings WHERE variable='" + pKey + "';";
            ResultSet rs = s.executeQuery(query);
            rs = s.executeQuery(query);
            while (rs.next()) {
                retVal = rs.getString("value");
            }
        } catch (Exception e) {
            logger.error("Failed to get property '" + pKey + "'", e);
            retVal = null;
        }
        return retVal;
    }

    public static List<DatabaseServerEntry> getServerList() {
        if (!openConnection()) {
            return null;
        }
        List<DatabaseServerEntry> retVal = new LinkedList<DatabaseServerEntry>();
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT * FROM update_daemon;";
            ResultSet rs = s.executeQuery(query);
            rs = s.executeQuery(query);
            while (rs.next()) {
                String id = rs.getString("serverID");
                String url = rs.getString("serverURL");
                int ver = rs.getInt("dataVersion");
                DatabaseServerEntry de = new DatabaseServerEntry();
                de.setServerID(id);
                de.setServerURL(url);
                de.setDataVersion(ver);
                retVal.add(de);
            }
        } catch (Exception e) {
            logger.error("Failed to obtain serverlist from database", e);
            retVal = null;
        }
        closeConnection();
        return retVal;
    }

    public static long getDataVersion(String pServerID) {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }
        long retVal = ID_UNKNOWN_ERROR;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String update = "SELECT * FROM update_daemon WHERE serverID = '" + pServerID + "';";
            ResultSet rs = s.executeQuery(update);

            while (rs.next()) {
                retVal = rs.getTimestamp("dataVersion").getTime();
            }
        } catch (Exception e) {
            logger.error("Failed to get data version for server '" + pServerID + "'", e);
            retVal = ID_UNKNOWN_ERROR;
        }
        closeConnection();
        return retVal;
    }

    public static String getServerDownloadURL(String pServerID) {
        if (!openConnection()) {
            return null;
        }
        String retVal = null;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT downloadURL FROM update_daemon WHERE ServerID='" + pServerID + "';";
            ResultSet rs = s.executeQuery(query);
            rs = s.executeQuery(query);
            while (rs.next()) {
                retVal = rs.getString("downloadURL");
            }
        } catch (Exception e) {
            logger.error("Failed to get downloadURL for server '" + pServerID + "'", e);
            retVal = null;
        }
        closeConnection();
        return retVal;
    }

    public static long getUserDataVersion(String pUsername, String pServer) {
        if (!openConnection()) {
            return ID_CONNECTION_FAILED;
        }
        long retVal = -1;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT dataVersion FROM updates LEFT JOIN users ON (updates.userID=users.id) WHERE (users.name='" + pUsername + "' AND updates.serverID ='" + pServer + "');";
            ResultSet rs = s.executeQuery(query);
            rs = s.executeQuery(query);
            boolean userRegistered = false;
            while (rs.next()) {
                retVal = rs.getTimestamp("dataVersion").getTime();
                userRegistered = true;
            }
            if (!userRegistered) {
                retVal = ID_UPDATE_NEVER;
            }
        } catch (Exception e) {
            logger.error("Failed to get dataVersion for user '" + pUsername + "' and server '" + pServer + "'", e);
            retVal = ID_UNKNOWN_ERROR;
        }
        closeConnection();
        return retVal;
    }

    public static boolean registerUserForServer(String pUsername, String pServer) {
        if (!openConnection()) {
            return false;
        }
        boolean retVal = false;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT id FROM users WHERE name='" + pUsername + "';";
            ResultSet rs = s.executeQuery(query);
            int id = ID_USER_NOT_EXIST;
            while (rs.next()) {
                id = rs.getInt("id");
            }

            if (id != ID_USER_NOT_EXIST) {
                String update = "INSERT INTO updates(userID, serverID, dataVersion) VALUES (" + id + ",'" + pServer + "',0);";
                int changed = s.executeUpdate(update);
                if (changed != 0) {
                    retVal = true;
                } else {
                    logger.warn("Failed to register user '" + pUsername + "' for server '" + pServer + "'");
                }
            } else {
                logger.warn("User '" + pUsername + "' does not exist");
            }
        } catch (Exception e) {
            logger.error("Failed to get dataVersion for user '" + pUsername + "' and server '" + pServer + "'", e);
            retVal = false;
        }
        closeConnection();
        return retVal;

    }

    public static boolean updateUserDataVersion(String pUsername, String pServer, long pVersion) {
        if (!openConnection()) {
            return false;
        }
        logger.debug("Updating user data version to " + pVersion);
        boolean retVal = false;
        try {
            Statement s = DB_CONNECTION.createStatement();
            String query = "SELECT id FROM users WHERE name='" + pUsername + "';";
            ResultSet rs = s.executeQuery(query);
            int id = ID_USER_NOT_EXIST;
            while (rs.next()) {
                id = rs.getInt("id");
            }

            if (id != ID_USER_NOT_EXIST) {

                s = DB_CONNECTION.createStatement();
                String update = "UPDATE updates SET dataVersion = '" + new Timestamp(pVersion).toString() + "' WHERE serverID = '" + pServer + "' AND userID = " + id + ";";
                int changed = s.executeUpdate(update);
                if (changed != 0) {
                    retVal = true;
                } else {
                    logger.warn("No rows changed");
                    retVal = false;
                }
            } else {
                logger.warn("User '" + pUsername + "' does not exist");
            }
        } catch (Exception e) {
            logger.error("Failed to update data version for user '" + pUsername + "' and server '" + pServer + "'", e);
            retVal = false;
        }
        closeConnection();
        return retVal;
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
                    if (v < 0) {
                        retVal = ID_UPDATE_NOT_ALLOWED;
                    } else if (Constants.VERSION < v) {
                        retVal = ID_VERSION_NOT_ALLOWED;
                    }
                } catch (Exception e) {
                    logger.error("Unknown min_version from server (" + min_version + ")");
                    retVal = ID_UNKNOWN_ERROR;
                }

            } else {
                retVal = ID_UNKNOWN_ERROR;
            }

        } catch (Exception e) {
            logger.error("Failed to check min version", e);
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
        System.setProperty("proxyUse", "true");
        System.setProperty("proxyHost", "proxy.fzk.de");
        System.setProperty("proxyPort", "8000");

        System.out.println(getUserDataVersion("Torridity", "de26"));

    //System.out.println(DatabaseAdapter.checkUser("Torridity", "realstyx13"));
    //System.out.println(DatabaseAdapter.getPropertyValue("update_base_dir"));
    // System.out.println(DatabaseAdapter.getUserDataVersion("Torridity", "de14"));
    //System.out.println(DatabaseAdapter.registerUserForServer("Torridity", "de14"));
    //System.out.println(DatabaseAdapter.getDataVersion("de14"));
    //System.out.println(DatabaseAdapter.setDataVersion("de14", 3));
    // System.out.println(DatabaseAdapter.getServerDownloadURL("de26"));
//System.out.println(DatabaseAdapter.updateUserDataVersion("Torridity", "de14", 0));
//System.out.println(Integer.parseInt(DatabaseAdapter.getPropertyValue("max_user_diff")));

    /*  System.out.println(DatabaseAdapter.addUser("Torridity", "realstyx13"));
    long s = System.currentTimeMillis();
    System.out.println(DatabaseAdapter.checkUser("Torridity", "realstyx13"));*/

    //  System.out.println(DatabaseAdapter.isUpdatePossible("Torridity", "de3"));
    /*System.out.println("Check: " + DatabaseAdapter.checkLastUpdate("Torridity", "de26"));
    System.out.println("Store: " + DatabaseAdapter.storeLastUpdate("Torridity", "de26"));
    System.out.println("Check: " + DatabaseAdapter.checkLastUpdate("Torridity", "de26"));*/

    // System.out.println("Du " + (System.currentTimeMillis() - s));
       /* String[] data = new String[]{"a1","A1", "b1", "b2", "B2", "A2", "B1", "c1", "c2"};
    List<String> d = new LinkedList<String>();
    for(String s : data){
    d.add(s);
    }
    /*StringComparator sc = new StringComparator();
    sc.setup(StringComparator.SORT_DESCENDING);*/
    //Collections.sort(d, String.CASE_INSENSITIVE_ORDER);
    //Arrays.sort(data, sc);
    // System.out.println(d);
    /*c2
    c1
    b2
    B2
    b1
    B1
    A2
    a1
    A1
     */
    }
}
