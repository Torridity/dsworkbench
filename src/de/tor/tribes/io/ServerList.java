/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 *
 * @author Charon
 */
public class ServerList {

    // private static Hashtable<String, URL> SERVER_LIST = null;
    private static Properties SERVERS = null;
    

    static {
        // SERVER_LIST = new Hashtable<String, URL>();
        SERVERS = new Properties();
    }

    public static void loadServerList() throws Exception {
        try {
            //URL serverURL = new URL("http://www.die-staemme.de/backend/get_servers.php");
            URLConnection con = new URL("http://www.torridity.de/servers/server.list").openConnection();
            SERVERS.load(con.getInputStream());
        } catch (Exception e) {
            throw new Exception("Serverliste konnte nicht heruntergeladen werden.\nBitte Netzwerkeinstellungen überprüfen.", e);
        }
    }

    public static String[] getServerIDs() {
        return (String[]) SERVERS.keySet().toArray(new String[0]);
    }

    public static URL getServerURL(String pServerID) {
        try {
            return new URL((String) SERVERS.get(pServerID));
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        ServerList.loadServerList();
    }
}
