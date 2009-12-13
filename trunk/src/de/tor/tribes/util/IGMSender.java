/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author Torridity
 */
public class IGMSender {

    public static boolean sendIGM(Tribe pReceiver, String pSubject, String pMessage) {
        Tribe t = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
        try {
            String text = URLEncoder.encode(pSubject, "UTF-8");
            String name = URLEncoder.encode(pReceiver.getName(), "UTF-8");
            String why = URLEncoder.encode(pMessage, "UTF-8");
            String apiKey = GlobalOptions.getProperty("api.key");
            String get = "http://de43.die-staemme.de/send_mail.php?from_id=" + t.getId() + "&api_key=" + apiKey;
            get += "&to=" + name + "&subject=" + why + "&message=" + text;
            URL u = new URL(get);
            URLConnection ucon = u.openConnection();
            String res = ucon.getHeaderField(0);
            return (res.endsWith("200 OK"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
