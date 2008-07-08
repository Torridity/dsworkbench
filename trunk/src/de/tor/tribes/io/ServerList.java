/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 * @author Charon
 */
public class ServerList {

    private static Hashtable<String, URL> SERVER_LIST = null;
    

    static {
        SERVER_LIST = new Hashtable<String, URL>();
    }

    public static void loadServerList() throws Exception {
        try {
            URL serverURL = new URL("http://www.die-staemme.de/backend/get_servers.php");
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverURL.openConnection().getInputStream()));
            String servers = reader.readLine();
            StringTokenizer t = new StringTokenizer(servers, "\"");
            t.nextToken();
            while (t.hasMoreTokens()) {
                String serverID = t.nextToken();
                t.nextToken();
                URL url = new URL(t.nextToken());
                SERVER_LIST.put(serverID, url);
                t.nextToken();
            }
        } catch (Exception e) {
            throw new Exception("Serverliste konnte nicht heruntergeladen werden.\nBitte Netzwerkeinstellungen überprüfen.");
        }
    }

    public static String[] getServerIDs() {
        return (String[]) SERVER_LIST.keySet().toArray(new String[0]);
    }

    public static URL getServerURL(String pServerID) {
        return SERVER_LIST.get(pServerID);
    }

    public static void main(String[] args) throws Exception {
        ServerList.loadServerList();
    }
}
