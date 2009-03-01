/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.apache.log4j.Logger;

/**Listening side on server -> only forwards data to other peers
 * @author Charon
 */
public class ComPeer extends Thread {

    private static Logger logger = Logger.getLogger("Client");
    private Socket sSocket = null;
    private BufferedReader inReader = null;
    private BufferedWriter outStream = null;
    private ComPeerListener mPeerListener = null;
    private boolean connected = false;
    private String sUserName = "unknown";
    private boolean disconnecting = false;

    public ComPeer(Socket pSocket) throws Exception {
        sSocket = pSocket;
        inReader = new BufferedReader(new InputStreamReader(pSocket.getInputStream()));
        outStream = new BufferedWriter(new OutputStreamWriter(pSocket.getOutputStream()));
        connected = true;
        setDaemon(true);
    }

    public void setUserName(String pName) {
        sUserName = pName;
    }

    public String getUserName() {
        return sUserName;
    }

    public void setComPeerListener(ComPeerListener pListener) {
        mPeerListener = pListener;
    }

    @Override
    public void run() {
        while (connected) {
            try {
                String line = inReader.readLine();
                if (line != null) {
                    mPeerListener.fireInputEvent(this, line);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                }

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public void sendOutput(String pData) {
        int attemps = 0;
        boolean error = false;
        while (attemps < 3) {
            try {
                outStream.write(pData);
                outStream.flush();
                try {
                    Thread.sleep(50);
                } catch (Exception inner) {
                }
                error = false;
                break;
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
                attemps++;
                try {
                    Thread.sleep(100);
                } catch (Exception inner) {
                }
            }
        }
        if (error) {
            if (!disconnecting) {
                System.out.println("Disconnecting");
                logger.error("Failed to send data. Disconnecting client");
                //behave as if peer has disconnected normally
                mPeerListener.fireInputEvent(this, "BYE;USER:" + sUserName);
            }
        }
    }

    public void sendBye() {
        String data = "DISCONNECT;";
        data += "\n";
        sendOutput(data);
    }

    public void sendKick() {
        String data = "KICK;";
        data += "\n";
        sendOutput(data);
    }

    protected void kick() {
        logger.debug("Kicking");
        sendKick();
        try {
            sSocket.close();
            inReader.close();
            outStream.close();
        } catch (Exception e) {
        }
        connected = false;
        mPeerListener.firePeerDisconnectedEvent(this);
    }

    public void sendBan() {
        String data = "BAN;";
        data += "\n";
        sendOutput(data);
    }

    protected void ban() {
        logger.debug("Baning");
        sendBan();
        try {
            sSocket.close();
            inReader.close();
            outStream.close();
        } catch (Exception e) {
        }
        connected = false;
        mPeerListener.firePeerDisconnectedEvent(this);
    }

    protected void disconnect() {
        disconnecting = true;
        logger.debug("Disconnecting");
        sendBye();
        try {
            sSocket.close();
            inReader.close();
            outStream.close();
        } catch (Exception e) {
        }
        connected = false;
        mPeerListener.firePeerDisconnectedEvent(this);
        disconnecting = false;
    }
}
