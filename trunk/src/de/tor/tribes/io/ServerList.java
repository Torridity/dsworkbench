/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.db.DatabaseAdapter;
import de.tor.tribes.db.DatabaseServerEntry;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class ServerList {

    // private static Hashtable<String, URL> SERVER_LIST = null;
    private static List<DatabaseServerEntry> SERVERS = null;
    

    static {
        // SERVER_LIST = new Hashtable<String, URL>();
        SERVERS = new LinkedList<DatabaseServerEntry>();
    }

    public static void loadServerList() throws Exception {
        SERVERS = DatabaseAdapter.getServerList();
    }

    public static String[] getServerIDs() {
        List<String> ids = new LinkedList<String>();
        for (DatabaseServerEntry entry : SERVERS) {
            ids.add(entry.getServerID());
        }
        String[] s = ids.toArray(new String[0]);
        Arrays.sort(s);
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

    public static void main(String[] args) throws Exception {
        ServerList.loadServerList();
    }
}
