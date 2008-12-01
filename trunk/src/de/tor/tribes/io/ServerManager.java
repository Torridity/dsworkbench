/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.db.DatabaseServerEntry;
import de.tor.tribes.php.DatabaseInterface;
import de.tor.tribes.util.Constants;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class ServerManager {

    // private static Hashtable<String, URL> SERVER_LIST = null;
    private static List<DatabaseServerEntry> SERVERS = null;
    

    static {
        SERVERS = new LinkedList<DatabaseServerEntry>();
        File serverDir = new File(Constants.SERVER_DIR);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
    }

    public static void loadServerList() throws Exception {
        SERVERS = DatabaseInterface.listServers();
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
}
