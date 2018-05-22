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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class ReportServer {

    private static Logger logger = Logger.getLogger("ReportServer");
    private static ReportServer SINGLETON = null;
    private RequestListenerThread serverThread = null;

    public static synchronized ReportServer getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ReportServer();
        }
        return SINGLETON;
    }

    public void start(int pPort) throws IOException {
        if (serverThread == null) {
            serverThread = new RequestListenerThread(pPort, "/");
            serverThread.setDaemon(false);
            serverThread.start();
        } else {
            logger.info("Server is already running");
        }
    }

    public void stop() {
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread.close();
            serverThread = null;
        }
    }

    static class HttpFileHandler implements HttpRequestHandler {

        public HttpFileHandler() {
            super();
        }

        @Override
        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {
            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            String target = request.getRequestLine().getUri();

            StringBuilder b = new StringBuilder();
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                logger.debug("Incoming entity content (bytes): " + entityContent.length);
                b.append(new String(entityContent));
            } else {
                //from GET request...no modification necessary
                b.append(request);
            }

            logger.debug("Accessing target " + target);

            if (target.contains("ajax.php?action=parse_report")) {
                String data = URLDecoder.decode(b.toString(), "UTF-8");
                String obstServer = GlobalOptions.getProperty("obst.server");
                if (obstServer != null && !obstServer.isEmpty()) {
                    obstServer += "/" + target;
                    try {
                        OBSTReportSender.sendReport(new URL(obstServer), b.toString());
                    } catch (Exception e) {
                        logger.error("Failed to forward report to OBST server " + obstServer, e);
                    }
                }

                response.setStatusCode(HttpStatus.SC_OK);
                EntityTemplate body;

                if (PluginManager.getSingleton().executeObstReportParser(data)) {
                    body = new EntityTemplate(new ContentProducer() {
                        @Override
                        public void writeTo(final OutputStream outstream) throws IOException {
                            OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                            writer.write("obstCallback(\"<response>"
                                    + "<message>The report has been parsed successfully.</message>"
                                    + "<reportid>0</reportid></data>"
                                    + "<data><error>0</error></data>"
                                    + "</response>"
                                    + "\");");
                            writer.flush();
                        }
                    });

                } else {
                    body = new EntityTemplate(new ContentProducer() {
                        @Override
                        public void writeTo(final OutputStream outstream) throws IOException {
                            OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                            writer.write("obstCallback(\"<response>"
                                    + "<message>The report could not be parsed.</message>"
                                    + "<data><error>2</error></data>"
                                    + "</response>"
                                    + "\");");
                            writer.flush();
                        }
                    });
                }
                body.setContentType("text/json; charset=UTF-8");
                response.setEntity(body);
            } else {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                EntityTemplate body = new EntityTemplate(new ContentProducer() {
                    @Override
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        writer.write("obstCallback(\"<response>"
                                + "<message>Invalid target URL</message>"
                                + "<data><error>3</error></data>"
                                + "</response>"
                                + "\");");
                        writer.flush();
                    }
                });
                body.setContentType("text/xml; charset=UTF-8");
                response.setEntity(body);
            }
        }
    }

    static class RequestListenerThread extends Thread {

        private final ServerSocket serversocket;
        private final HttpParams params;
        private final HttpService httpService;

        public RequestListenerThread(int port, final String docroot) throws IOException {
            this.serversocket = new ServerSocket(port);
            this.params = new SyncBasicHttpParams();
            this.params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000).setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024).setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true).setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            // Set up the HTTP protocol processor
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[]{
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
            });

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", new HttpFileHandler());

            // Set up the HTTP service
            this.httpService = new HttpService(
                    httpproc,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory(),
                    reqistry,
                    this.params);
        }

        public void close() {
            try {
                this.serversocket.close();
            } catch (Exception ignored) {
            }
        }

        @Override
        public void run() {
            logger.debug("Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    logger.debug("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, this.params);
                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    logger.error("I/O error initialising connection thread: " + e.getMessage(), e);
                    break;
                }
            }
        }
    }

    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        public WorkerThread(
                final HttpService httpservice,
                final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        @Override
        public void run() {
            logger.debug("New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                logger.error("Client closed connection");
            } catch (IOException ex) {
                logger.error("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                logger.error("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ReportServer.getSingleton().start(80);
    }
}

//package de.tor.tribes.util;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.InterruptedIOException;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.security.KeyManagementException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
//import java.util.Locale;
//import java.util.logging.Level;
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLServerSocket;
//import javax.net.ssl.SSLServerSocketFactory;
//import javax.net.ssl.SSLSocket;
//
//import org.apache.http.ConnectionClosedException;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpEntityEnclosingRequest;
//import org.apache.http.HttpException;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpResponseInterceptor;
//import org.apache.http.HttpServerConnection;
//import org.apache.http.HttpStatus;
//import org.apache.http.MethodNotSupportedException;
//import org.apache.http.entity.ContentProducer;
//import org.apache.http.entity.EntityTemplate;
//import org.apache.http.impl.DefaultConnectionReuseStrategy;
//import org.apache.http.impl.DefaultHttpResponseFactory;
//import org.apache.http.impl.DefaultHttpServerConnection;
//import org.apache.http.params.CoreConnectionPNames;
//import org.apache.http.params.HttpParams;
//import org.apache.http.params.CoreProtocolPNames;
//import org.apache.http.params.SyncBasicHttpParams;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.protocol.BasicHttpContext;
//import org.apache.http.protocol.HttpProcessor;
//import org.apache.http.protocol.HttpRequestHandler;
//import org.apache.http.protocol.HttpRequestHandlerRegistry;
//import org.apache.http.protocol.HttpService;
//import org.apache.http.protocol.ImmutableHttpProcessor;
//import org.apache.http.protocol.ResponseConnControl;
//import org.apache.http.protocol.ResponseContent;
//import org.apache.http.protocol.ResponseDate;
//import org.apache.http.protocol.ResponseServer;
//import org.apache.http.util.EntityUtils;
//import org.apache.log4j.Logger;
//
///**
// * This does not work currently due to the missing HTTPS support of the internal
// * server implementation. First steps are made, but getting and parsing the data
// * from HTTPs stream was never finished.
// *
// * @author Torridity
// */
//public class ReportServer {
//
//    private static Logger logger = Logger.getLogger("ReportServer");
//    private static ReportServer SINGLETON = null;
//    // private RequestListenerThread serverThread = null;
//    private SSLWorkerThread serverThread;
//
//    public ReportServer() {
//    }
//
//    public static synchronized ReportServer getSingleton() {
//        if (SINGLETON == null) {
//            SINGLETON = new ReportServer();
//        }
//        return SINGLETON;
//    }
//
//    public void start(int pPort) throws IOException {
//        if (serverThread == null) {
//            //keystore including the key for HTTPs connection
//            String ksName = "dsworkbench.jks";
//            char ksPass[] = "dsworkbench".toCharArray();
//            char ctPass[] = "dsworkbench".toCharArray();
//            try {
//                KeyStore ks = KeyStore.getInstance("JKS");
//                ks.load(new FileInputStream(ksName), ksPass);
//                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//                kmf.init(ks, ctPass);
//                SSLContext sc = SSLContext.getInstance("TLS");
//                sc.init(kmf.getKeyManagers(), null, null);
//                SSLServerSocketFactory ssf = sc.getServerSocketFactory();
//                SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(pPort);
//                s.setEnabledCipherSuites(sc.getServerSocketFactory().getSupportedCipherSuites());
//                serverThread = new SSLWorkerThread(s);
//                serverThread.start();
//            } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | KeyManagementException | UnrecoverableKeyException ex) {
//                logger.error("Failed to decrypt SSL key.", ex);
//            }
//        } else {
//            logger.info("Server is already running");
//        }
//    }
//
//    public void stop() {
//        if (serverThread != null) {
//            serverThread.stopServer();
//            serverThread.interrupt();
//            serverThread.close();
//            serverThread = null;
//        }
//    }
//
//    static class SSLWorkerThread extends Thread {
//
//        private final SSLServerSocket serverSocket;
//        private boolean running = true;
//        private String lineSeparator;
//
//        public SSLWorkerThread(SSLServerSocket pSocket) {
//            serverSocket = pSocket;
//            lineSeparator = System.getProperty("line.separator");
//            setDaemon(true);
//            setPriority(MIN_PRIORITY);
//        }
//
//        @Override
//        public void run() {
//            while (running) {
//                try {
//                    System.out.println("ACC");
//                    try (SSLSocket connectedSocket = (SSLSocket) serverSocket.accept()) {
//                        logger.debug("Accepted connection.");
//                        BufferedReader r;
//                        System.out.println("WR");
//                        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(connectedSocket.getOutputStream()))) {
//                            r = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
//                            System.out.println("READ");
//                            String line;
//                            StringBuilder content = new StringBuilder();
//                            logger.debug("Reading content from socket connection.");
//                            w.write("Ready.");
//                            w.newLine();
//                            w.flush();
//                            while ((line = r.readLine()) != null) {
//                                System.out.println("NEXT " + line);
//                                content.append(line).append("\r\n");
//                            }
//                            System.out.println("PARSE");
//                            logger.debug("Trying to parse content.");
//                            if (OBSTReportHandler.handleReport(content.toString())) {
//                                System.out.println("OK!! ");
//                                logger.debug("Successfully parsed report. Sending response.");
//                                w.write("HTTP/1.0 200 OK");
//                                w.newLine();
//                                w.write("Content-Type: text/json; charset=UTF-8");
//                                w.newLine();
//                                w.newLine();
//                                w.write("obstCallback(\"<response>"
//                                        + "<message>The report has been parsed successfully.</message>"
//                                        + "<reportid>0</reportid></data>"
//                                        + "<data><error>0</error></data>"
//                                        + "</response>"
//                                        + "\");");
//                                w.newLine();
//                            } else {
//                                System.out.println("ERR");
//                                logger.debug("Failed to parse report. Sending response.");
//                                w.write("HTTP/1.0 200 OK");
//                                w.newLine();
//                                w.write("Content-Type: text/json; charset=UTF-8");
//                                w.newLine();
//                                w.newLine();
//                                w.write("obstCallback(\"<response>"
//                                        + "<message>The report could not be parsed.</message>"
//                                        + "<data><error>2</error></data>"
//                                        + "</response>"
//                                        + "\");");
//                                w.newLine();
//                            }
//                            System.out.println("FLUSH");
//                            w.flush();
//                        }
//                        r.close();
//                    }
//                } catch (IOException ex) {
//                    logger.error("Failed to accept report connection", ex);
//                }
//            }
//        }
//
//        public void stopServer() {
//            running = false;
//        }
//
//        public void close() {
//            try {
//                serverSocket.close();
//            } catch (IOException ex) {
//                logger.warn("Failed to close server thread", ex);
//            }
//        }
//    }
//
//    static class HttpFileHandler implements HttpRequestHandler {
//
//        public HttpFileHandler() {
//            super();
//        }
//
//        public void handle(
//                final HttpRequest request,
//                final HttpResponse response,
//                final HttpContext context) throws HttpException, IOException {
//            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
//            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
//                throw new MethodNotSupportedException(method + " method not supported");
//            }
//            String target = request.getRequestLine().getUri();
//
//            StringBuilder b = new StringBuilder();
//            if (request instanceof HttpEntityEnclosingRequest) {
//                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
//                byte[] entityContent = EntityUtils.toByteArray(entity);
//                logger.debug("Incoming entity content (bytes): " + entityContent.length);
//                b.append(new String(entityContent));
//            } else {
//                //from GET request...no modification necessary
//                b.append(request);
//            }
//
//            logger.debug("Accessing target " + target);
//
//            if (target.contains("ajax.php?action=parse_report")) {
//                String data = URLDecoder.decode(b.toString(), "UTF-8");
//                String obstServer = GlobalOptions.getProperty("obst.server");
//                if (obstServer != null && !obstServer.equals("")) {
//                    obstServer += "/" + target;
//                    try {
//                        OBSTReportSender.sendReport(new URL(obstServer), b.toString());
//                    } catch (Exception e) {
//                        logger.error("Failed to forward report to OBST server " + obstServer, e);
//                    }
//                }
//
//                response.setStatusCode(HttpStatus.SC_OK);
//                EntityTemplate body;
//
//                if (OBSTReportHandler.handleReport(data)) {
//                    body = new EntityTemplate(new ContentProducer() {
//                        public void writeTo(final OutputStream outstream) throws IOException {
//                            OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
//                            writer.write("obstCallback(\"<response>"
//                                    + "<message>The report has been parsed successfully.</message>"
//                                    + "<reportid>0</reportid></data>"
//                                    + "<data><error>0</error></data>"
//                                    + "</response>"
//                                    + "\");");
//                            writer.flush();
//                        }
//                    });
//                } else {
//                    body = new EntityTemplate(new ContentProducer() {
//                        public void writeTo(final OutputStream outstream) throws IOException {
//                            OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
//                            writer.write("obstCallback(\"<response>"
//                                    + "<message>The report could not be parsed.</message>"
//                                    + "<data><error>2</error></data>"
//                                    + "</response>"
//                                    + "\");");
//                            writer.flush();
//                        }
//                    });
//                }
//                body.setContentType("text/json; charset=UTF-8");
//                response.setEntity(body);
//            } else {
//                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
//                EntityTemplate body = new EntityTemplate(new ContentProducer() {
//                    @Override
//                    public void writeTo(final OutputStream outstream) throws IOException {
//                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
//                        writer.write("obstCallback(\"<response>"
//                                + "<message>Invalid target URL</message>"
//                                + "<data><error>3</error></data>"
//                                + "</response>"
//                                + "\");");
//                        writer.flush();
//                    }
//                });
//                body.setContentType("text/xml; charset=UTF-8");
//                response.setEntity(body);
//            }
//        }
//    }
//
//    static class RequestListenerThread extends Thread {
//
//        private final ServerSocket serversocket;
//        private final HttpParams params;
//        private final HttpService httpService;
//
//        public RequestListenerThread(int port, final String docroot) throws IOException {
//            this.serversocket = new ServerSocket(port);
//            this.params = new SyncBasicHttpParams();
//            this.params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000).setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024).setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true).setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
//
//            // Set up the HTTP protocol processor
//            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[]{
//                new ResponseDate(),
//                new ResponseServer(),
//                new ResponseContent(),
//                new ResponseConnControl()
//            });
//
//            // Set up request handlers
//            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
//            reqistry.register("*", new HttpFileHandler());
//
//            // Set up the HTTP service
//            this.httpService = new HttpService(
//                    httpproc,
//                    new DefaultConnectionReuseStrategy(),
//                    new DefaultHttpResponseFactory(),
//                    reqistry,
//                    this.params);
//        }
//
//        public void close() {
//            try {
//                this.serversocket.close();
//            } catch (Exception e) {
//            }
//        }
//
//        public void run() {
//            logger.debug("Listening on port " + this.serversocket.getLocalPort());
//            while (!Thread.interrupted()) {
//                try {
//                    // Set up HTTP connection
//                    Socket socket = this.serversocket.accept();
//                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
//                    logger.debug("Incoming connection from " + socket.getInetAddress());
//                    conn.bind(socket, this.params);
//                    // Start worker thread
//                    Thread t = new WorkerThread(this.httpService, conn);
//                    t.setDaemon(true);
//                    t.start();
//                } catch (InterruptedIOException ex) {
//                    break;
//                } catch (IOException e) {
//                    logger.error("I/O error initialising connection thread: " + e.getMessage(), e);
//                    break;
//                }
//            }
//        }
//    }
//
//    static class WorkerThread extends Thread {
//
//        private final HttpService httpservice;
//        private final HttpServerConnection conn;
//
//        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
//            super();
//            this.httpservice = httpservice;
//            this.conn = conn;
//        }
//
//        public void run() {
//            logger.debug("New connection thread");
//            HttpContext context = new BasicHttpContext(null);
//            try {
//                while (!Thread.interrupted() && this.conn.isOpen()) {
//                    this.httpservice.handleRequest(this.conn, context);
//                }
//            } catch (ConnectionClosedException ex) {
//                logger.error("Client closed connection");
//            } catch (IOException ex) {
//                logger.error("I/O error: " + ex.getMessage());
//            } catch (HttpException ex) {
//                logger.error("Unrecoverable HTTP protocol violation: " + ex.getMessage());
//            } finally {
//                try {
//                    this.conn.shutdown();
//                } catch (IOException ignore) {
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        ReportServer.getSingleton().start(8000);
//    }
//}
