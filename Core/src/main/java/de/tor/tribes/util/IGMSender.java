/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class IGMSender {

    private static Logger logger = Logger.getLogger("IGMSender");

    public static boolean sendIGM(Tribe pReceiver, String pApiKey, String pSubject, String pMessage) {
        Tribe t = GlobalOptions.getSelectedProfile().getTribe();
        try {
            String why = URLEncoder.encode(pSubject, "UTF-8");
            String name = URLEncoder.encode(pReceiver.getName(), "UTF-8");
            String text = URLEncoder.encode(pMessage, "UTF-8");
            String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String get = serverURL + "/send_mail.php?from_id=" + t.getId() + "&api_key=" + pApiKey;
            get += "&to=" + name + "&subject=" + why + "&message=" + text;
            URL u = new URL(get);
            URLConnection ucon = u.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
            String res = ucon.getHeaderField(0);
            byte[] data = new byte[1024];
            int read = 0;
            String returnString = "";
            while (read != -1) {
                read = ucon.getInputStream().read(data);
                returnString += new String(data).trim();
                data = new byte[1024];
            }

            boolean headerCorrect = res.endsWith("200 OK");
            if (!headerCorrect) {
                throw new Exception("Invalid HTTP header returned (" + res + ")");
            }
            boolean returnStringCorrect = returnString.startsWith("Nachricht erfolgreich");
            if (!returnStringCorrect) {
                throw new Exception("Invalid API response returned ('" + returnString + "')");
            }
            return (headerCorrect && returnStringCorrect);
        } catch (Exception e) {
            logger.error("Failed to send IGM", e);
            return false;
        }
    }

    public static boolean sendIGM(String pReceiver, String pApiKey, String pSubject, String pMessage) {
        try {
            String text = URLEncoder.encode(pSubject, "UTF-8");
            String name = URLEncoder.encode(pReceiver, "UTF-8");
            String why = URLEncoder.encode(pMessage, "UTF-8");
            String apiKey = pApiKey;
            String serverURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            String get = serverURL + "/send_mail.php?from_id=3457919&api_key=" + apiKey;
            get += "&to=" + name + "&subject=" + why + "&message=" + text;
            URL u = new URL(get);
            Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.fzk.de", 8000));
            URLConnection ucon = u.openConnection(p);
            String res = ucon.getHeaderField(0);
            byte[] data = new byte[1024];
            int read = 0;
            String returnString = "";
            while (read != -1) {
                read = ucon.getInputStream().read(data);
                returnString += new String(data).trim();
                data = new byte[1024];
            }

            return (returnString.startsWith("Nachricht erfolgreich"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
