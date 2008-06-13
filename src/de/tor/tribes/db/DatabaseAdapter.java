/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

import de.tor.tribes.sec.SecurityAdapter;
import de.tor.tribes.util.GlobalLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

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
    

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            logger.error("Database driver not found");
        }
    }

    private static boolean openConnection() {
        try {
            DB_CONNECTION = DriverManager.getConnection("jdbc:mysql://www.torridity.de/dsworkbench?" + "user=dsworkbench&password=DSwb'08");
            return true;
        } catch (SQLException se) {
            logger.error("Failed to establish database connection", se);
            return false;
        }
    }

    private static void closeConnection() {
        try {
            DB_CONNECTION.close();
        } catch (Throwable t) {
        }
    }

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
                String update = "INSERT INTO users(name, password, lastupdate) VALUES ('" + pUsername + "', '" + SecurityAdapter.hashStringMD5(pPassword) + "', 0);";
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

    public static void main(String[] args) {
        System.out.println(DatabaseAdapter.addUser("Torridity", "realstyx13"));
        long s = System.currentTimeMillis();
        System.out.println(DatabaseAdapter.checkUser("Torridity", "realstyx13"));
        System.out.println("Du " + (System.currentTimeMillis() - s));
    }
}
