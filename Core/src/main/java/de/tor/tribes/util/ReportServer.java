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
 * This does not work currently due to the missing HTTPS support of the internal
 * server implementation. First steps are made, but getting and parsing the data
 * from HTTPs stream was never finished.
 *
 * @author Torridity
 */
public class ReportServer {
    //TODO check if this thing works
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
                String line; int tmp;
                String head = "";
                StringBuilder content = new StringBuilder();
                logger.debug("Reading content from socket connection.");
                w.write("Ready.");
                w.newLine();
                w.flush();
                while ((tmp = r.read()) != -1  && head.contains("\r\n\r\n")) {
                        head += ((char) tmp);
                }
                logger.debug("Header: " + head);
                while ((line = r.readLine()) != null) {
                    logger.debug("Current content line: " + line);
                        content.append(line).append("\r\n");
                }
                logger.debug("Trying to parse content.");
                if (PluginManager.getSingleton().executeObstReportParser(content.toString())) {
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
                
                //Find target Field in Header
                String target = "";
                String requestParams[] = head.replaceAll("\r", "").split("\n");
                for(String requestParam: requestParams) {
                    if(requestParam.startsWith("GET")) {
                        target = requestParam.substring(3).trim();
                    }
                }
                
                logger.debug("Accessing target " + target);
                if (target.contains("ajax.php?action=parse_report")) {
                    String obstServer = GlobalOptions.getProperty("obst.server");
                    if (obstServer != null && !obstServer.isEmpty()) {
                        obstServer += "/" + target;
                        try {
                            OBSTReportSender.sendReport(new URL(obstServer), content.toString());
                        } catch (Exception e) {
                            logger.error("Failed to forward report to OBST server " + obstServer, e);
                        }
                    }
                } else if(target.equals("")) {
                    logger.error("Failed to get Request target " + head);
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
