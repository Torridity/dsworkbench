/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class ComServer extends Thread {

    private static Logger logger = Logger.getLogger("Server");
    private ServerSocket sSocket = null;
    private ComServerListener mServerListener = null;
    private boolean connected = false;

    public ComServer(int pPort) throws Exception {
        sSocket = new ServerSocket(pPort);
        connected = true;
        setDaemon(true);
    }

    public void setComServerListener(ComServerListener pListener) {
        mServerListener = pListener;
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Socket client = sSocket.accept();
                logger.debug("Got new peer connection");
                ComPeer peer = new ComPeer(client);
                peer.start();
                mServerListener.fireClientConnectionEvent(peer);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }

            } catch (Exception e) {
            }
        }
    }

    public void stopServer() {
        connected = false;
        try {
            sSocket.close();
        } catch (Throwable t) {
        }
    }
}
