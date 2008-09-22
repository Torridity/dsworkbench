/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.db;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import de.tor.tribes.types.Village;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Charon
 */
public class DBImporter {

    static {
        //try to load the MySQL driver
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

        } catch (Exception ex) {
        }
    }
    private static Connection DB_CONNECTION = null;

    /**Open the database connection*/
    private static boolean openConnection() {
        try {//DSwb'08
            //&password=test123
            DB_CONNECTION = (Connection) DriverManager.getConnection("jdbc:mysql://62.75.170.38:3306/dsworkbench?" + "user=dsworkbench&password=DSwb'08");
            return true;
        } catch (SQLException se) {
            se.printStackTrace();
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

    public void importToDB() throws Exception {
        if (!openConnection()) {
            return;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./servers/de26/village.txt.gz"))));
        String line = "";
        int cnt = 0;
        int v = 0;
        String block = "";
        long t = 0;
        long start = System.currentTimeMillis();
        long s1 = System.currentTimeMillis();
        String update = "INSERT INTO `villages` (`id`, `name`, `x`, `y`, `tribe`, `points`, `type`) VALUES ";
        while ((line = r.readLine()) != null) {

            block += parseVillage(line) + ",";
            if (cnt == 500) {

                Statement s = (Statement) DB_CONNECTION.createStatement();
                String data = update + block;
                data = data.substring(0, data.length() - 1);
                data += ";";
                // System.out.println(data);
                s.executeUpdate(data);

                block = "";
                cnt = 0;
                System.out.println("Inserted block");

            }
            cnt++;

        /*if (update.length() >= 1900000) {
        update = update.substring(0, update.length() - 1);
        update += ";";
        FileWriter f = new FileWriter(new File("c:/data.sql"));
        f.write(update);
        f.close();
        System.exit(0);
        }*/

        }

        closeConnection();
        System.out.println("Dur " + (System.currentTimeMillis() - s1));
    }

    private String parseVillage(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");

        Village entry = new Village();
        if (tokenizer.countTokens() < 7) {
            return "";
        }

        try {
            String data = "(" +
                    tokenizer.nextToken() + ", '" +
                    tokenizer.nextToken() + "'," +
                    tokenizer.nextToken() + "," +
                    tokenizer.nextToken() + "," +
                    tokenizer.nextToken() + "," +
                    tokenizer.nextToken() + "," +
                    tokenizer.nextToken() + ")";
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void select() {
        openConnection();
        try {
            long st = System.currentTimeMillis();
            String update = "SELECT * FROM `villages` WHERE (`x`>500 AND `x`<550 AND `y`>500 AND `y`<550);";
            Statement s = (Statement) DB_CONNECTION.createStatement();
            
            s.executeQuery(update);
            System.out.println("dur: " + (System.currentTimeMillis() - st));
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeConnection();
    }

   /* public static void main(String[] args) throws Exception {
        new DBImporter().select();
    }*/
}
