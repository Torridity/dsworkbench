/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author Torridity
 */
public class IGMSender {

    public static boolean sendIGM(Tribe pReceiver, String pApiKey, String pSubject, String pMessage) {
        Tribe t = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
        try {
            String text = URLEncoder.encode(pSubject, "UTF-8");
            String name = URLEncoder.encode(pReceiver.getName(), "UTF-8");
            String why = URLEncoder.encode(pMessage, "UTF-8");
            String apiKey = pApiKey;
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

    public static boolean sendIGM(String pReceiver, String pApiKey, String pSubject, String pMessage) {

        try {
            String text = URLEncoder.encode(pSubject, "UTF-8");
            String name = URLEncoder.encode(pReceiver, "UTF-8");
            String why = URLEncoder.encode(pMessage, "UTF-8");
            String apiKey = pApiKey;
            String get = "http://de43.die-staemme.de/send_mail.php?from_id=3457919&api_key=" + apiKey;
            get += "&to=" + name + "&subject=" + why + "&message=" + text;
            System.out.println(get);
            URL u = new URL(get);
            URLConnection ucon = u.openConnection();
            String res = ucon.getHeaderField(0);
            byte[] data = new byte[1024];
            ucon.getInputStream().read(data);
            String response = new String(data).trim();
            return (response.startsWith(""));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(IGMSender.sendIGM("Rattenfutter", "64ef1ebccdfd2b1e345a3514f2569419420b024c", "Test", "test"));
    }
}
