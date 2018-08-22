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
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Torridity
 */
public class ReportServer {
    private static Logger logger = LogManager.getLogger("ReportServer");
    private static ReportServer SINGLETON = null;
    private SSLWorkerThread sslWorkerThread;

    public ReportServer() {
    }

    public static synchronized ReportServer getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ReportServer();
        }
        return SINGLETON;
    }

    public void start(int pPort) throws IOException {
        if (sslWorkerThread == null) {
            //keystore including the key for HTTPs connection
            String ksName = "dsworkbench.jks";
            char ksPass[] = "dsworkbench".toCharArray();
            char ctPass[] = "dsworkbench".toCharArray();
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(ksName), ksPass);
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, ctPass);
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(kmf.getKeyManagers(), null, null);
                SSLServerSocketFactory ssf = sc.getServerSocketFactory();
                SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(pPort);
                s.setEnabledCipherSuites(sc.getServerSocketFactory().getSupportedCipherSuites());
                sslWorkerThread = new SSLWorkerThread(s);
                sslWorkerThread.start();
            } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | KeyManagementException | UnrecoverableKeyException ex) {
                logger.error("Failed to decrypt SSL key.", ex);
            }
        } else {
            logger.info("Server is already running");
        }
    }

    public void stop() {
        if (sslWorkerThread != null) {
            sslWorkerThread.stopServer();
            sslWorkerThread.interrupt();
            sslWorkerThread.close();
            sslWorkerThread = null;
        }
    }

    static class SSLWorkerThread extends Thread {

        private final SSLServerSocket serverSocket;
        private boolean running = true;
        private String lineSeparator;

        public SSLWorkerThread(SSLServerSocket pSocket) {
            serverSocket = pSocket;
            lineSeparator = System.getProperty("line.separator");
            setDaemon(true);
            setPriority(MIN_PRIORITY);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    SSLSocket connectedSocket = (SSLSocket) serverSocket.accept();
                    new Thread(new RequestHanlder(connectedSocket)).start();
                } catch (IOException ex) {
                    logger.error("Failed to accept report connection", ex);
                }
            }
        }

        public void stopServer() {
            running = false;
        }

        public void close() {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                logger.warn("Failed to close server thread", ex);
            }
        }
    }
    
    private static class RequestHanlder implements Runnable {
        SSLSocket connectedSocket;
                
        RequestHanlder(SSLSocket pConnectedSocket) {
            connectedSocket = pConnectedSocket;
        }

        @Override
        public void run() {
            logger.debug("Accepted connection.");
            BufferedReader r;
            try {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(connectedSocket.getOutputStream()));
                r = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
                int tmp;
                String head = "";
                logger.debug("Reading content from socket connection.");
                while (!head.contains("\r\n\r\n") && (tmp = r.read()) != -1) {
                        head += ((char) tmp);
                }
                logger.debug("Header: " + head);
                
                logger.debug("Trying to parse Header.");
                //we need the request URI
                String splited[] = head.replaceAll("\r", "").split("\n");
                String url = null;
                for(String part: splited) {
                    if(part.contains("GET"))
                        url = part;
                }
                
                if(url == null) {
                    //not found --> log and return
                    logger.warn("No URL found:\n {}", head);
                    try {
                        connectedSocket.close();
                    } catch (IOException ignored) {
                    }
                    return;
                }
                url = url.split(" ")[1];
                url = url.substring((url.startsWith("/")?1:0));
                
                String report = null;
                //workaround for finding start & end because "&", "/" are not escaped in URL
                report = url.substring(url.indexOf("&report=") + 8, url.indexOf("&user="));
                
                report = URLDecoder.decode(report, "UTF-8");
                logger.debug("Report raw: {}", report);

                if (PluginManager.getSingleton().executeObstReportParser(report)) {
                    logger.debug("Successfully parsed report. Sending response.");
                    w.write("HTTP/1.0 200 OK");
                    w.newLine();
                    w.write("Content-Type: text/json; charset=UTF-8");
                    w.newLine();
                    w.newLine();
                    w.write("obstCallback(\"<response>"
                            + "<message>The report has been parsed successfully.</message>"
                            + "<reportid>0</reportid></data>"
                            + "<data><error>0</error></data>"
                            + "</response>"
                            + "\");");
                    w.newLine();
                } else {
                    logger.debug("Failed to parse report. Sending response.");
                    w.write("HTTP/1.0 200 OK");
                    w.newLine();
                    w.write("Content-Type: text/json; charset=UTF-8");
                    w.newLine();
                    w.newLine();
                    w.write("obstCallback(\"<response>"
                            + "<message>The report could not be parsed.</message>"
                            + "<data><error>2</error></data>"
                            + "</response>"
                            + "\");");
                    w.newLine();
                }
                w.flush();
                r.close();
                connectedSocket.close();
                
                logger.debug("Accessing target " + url);
                if (url.contains("ajax.php?action=parse_report")) {
                    String obstServer = GlobalOptions.getProperty("obst.server");
                    if (obstServer != null && !obstServer.isEmpty()) {
                        obstServer += "/" + url;
                        try {
                            OBSTReportSender.sendReport(new URL(obstServer));
                        } catch (Exception e) {
                            logger.error("Failed to forward report to OBST server " + obstServer, e);
                        }
                    }
                }
            }
            catch(IOException e) {
                logger.warn("Failed fetching OBST Report", e);
                try {
                    connectedSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
