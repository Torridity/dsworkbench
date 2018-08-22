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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class OBSTReportSender {
    private static final Logger logger = LogManager.getLogger("OBSTReportSender");
    //TODO check if this works
    //TODO check return of Obst-Server
    public static void sendReport(URL pTarget) throws Exception {
        HttpURLConnection connection = null;

        try {
            //Create connection
            connection = (HttpURLConnection) pTarget.openConnection();
            connection.setRequestMethod("GET");
            /*connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", 
                "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", 
                Integer.toString(pData.getBytes().length));
            connection.setRequestProperty("Content-Language", "de-DE");  

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                connection.getOutputStream());
            wr.writeBytes(pData);
            wr.close(); */

            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            //return response.toString();
        } catch (Exception e) {
            logger.warn("Unable to send Report", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
