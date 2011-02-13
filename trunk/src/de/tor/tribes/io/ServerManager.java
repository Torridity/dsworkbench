/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.php.DatabaseInterface;
import de.tor.tribes.util.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *@TODO (DIFF) Allowed to add additional servers via local servers.txt
 * @author Charon
 */
public class ServerManager {

    private static Logger logger = Logger.getLogger("ServerManager");
    // private static Hashtable<String, URL> SERVER_LIST = null;
    private static List<DatabaseServerEntry> SERVERS = null;
    private static boolean SERVERS_UPDATED = false;

    static {
        SERVERS = new LinkedList<DatabaseServerEntry>();
        File serverDir = new File(Constants.SERVER_DIR);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
    }

    public static void loadServerList() throws Exception {
        if (SERVERS_UPDATED) {
            //don't reload servers every time
            return;
        }
        SERVERS = DatabaseInterface.getServerInfo();
        /*    DatabaseServerEntry el = new DatabaseServerEntry();
        el.setServerID("de55");
        el.setServerURL("http://de55.die-staemme.de");
        el.setAcceptanceRiseSpeed(1.0);
        el.setDataVersion(0);
        el.setNightBonus((byte) 1);
        el.setDecoration(0);
        SERVERS.add(el);*/
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader("servers.txt"));
            int cnt = 0;
            String line = "";
            while ((line = r.readLine()) != null) {
                String[] split = line.split(";");
                DatabaseServerEntry el = new DatabaseServerEntry();
                el.setServerID(split[0]);
                el.setServerURL(split[1]);
                el.setAcceptanceRiseSpeed(Double.parseDouble(split[2]));
                el.setDataVersion(0);
                el.setNightBonus(Byte.parseByte(split[3]));
                try {
                    el.setDecoration(Integer.parseInt(split[4]));
                } catch (Exception ignored) {
                }
                SERVERS.add(el);
                cnt++;
            }
            logger.info("Read " + cnt + " external servers");
        } catch (Throwable e) {
            logger.error("Failed to read external servers", e);
        } finally {
            try {
                r.close();
            } catch (Exception ignore) {
            }
        }
        if (SERVERS != null && !SERVERS.isEmpty()) {
            SERVERS_UPDATED = true;
        }
    }

    /**Get the listof locally stored servers*/
    public static String[] getLocalServers() {
        List<String> servers = new LinkedList<String>();
        for (File serverDir : new File(Constants.SERVER_DIR).listFiles()) {
            if (serverDir.isDirectory()) {
                servers.add(serverDir.getName());
            }
        }
        return servers.toArray(new String[0]);
    }

    public static String[] getServerIDs() {
        List<String> ids = new LinkedList<String>();
        for (DatabaseServerEntry entry : SERVERS) {
            ids.add(entry.getServerID());
        }
        String[] s = ids.toArray(new String[]{});
        Arrays.sort(s, String.CASE_INSENSITIVE_ORDER);
        return s;
    }

    public static String getServerURL(String pServerID) {
        for (DatabaseServerEntry entry : SERVERS) {
            if (entry.getServerID().equals(pServerID)) {
                return entry.getServerURL();
            }
        }
        return null;
    }

    public static double getServerAcceptanceRiseSpeed(String pServerID) {
        for (DatabaseServerEntry entry : SERVERS) {
            if (entry.getServerID().equals(pServerID)) {
                return entry.getAcceptanceRiseSpeed();
            }
        }
        return 1;
    }

    public static byte getNightBonusRange(String pServerID) {
        for (DatabaseServerEntry entry : SERVERS) {
            if (entry.getServerID().equals(pServerID)) {
                return entry.getNightBonus();
            }
        }
        return DatabaseServerEntry.NIGHT_BONUS_0to8;
    }

    public static int getServerDecoration(String pServerID) {
        for (DatabaseServerEntry entry : SERVERS) {
            if (entry.getServerID().equals(pServerID)) {
                return entry.getDecoration();
            }
        }
        return 0;
    }
}
