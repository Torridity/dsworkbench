/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

import de.tor.tribes.util.Constants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class ComClient extends Thread implements CommandHandlerListener {

    private static Logger logger = Logger.getLogger("ComClient");
    private Socket mClientSocket = null;
    private BufferedReader inReader = null;
    private BufferedWriter outStream = null;
    private CommandHandler mHandler = null;
    private boolean connected = false;
    private List<ComClientListener> mListeners = null;
    private String sNick = "unknown";

    public ComClient(String pServer, int pPort, String pNick) throws Exception {
        //mClientSocket = new Socket(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
        sNick = pNick;
        mClientSocket = new Socket();
        SocketAddress sockaddr = new InetSocketAddress(pServer, pPort);
        mClientSocket.connect(sockaddr);
        connected = true;
        inReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
        outStream = new BufferedWriter(new OutputStreamWriter(mClientSocket.getOutputStream()));
        mHandler = new CommandHandler(this);
        mListeners = new LinkedList<ComClientListener>();
        setDaemon(true);
        start();
    }

    public String getNick() {
        return sNick;
    }

    public synchronized void addComClientListener(ComClientListener pListener) {
        mListeners.add(pListener);
    }

    public synchronized void removeComClientListener(ComClientListener pListener) {
        mListeners.remove(pListener);
    }

    @Override
    public void run() {
        while (connected) {
            try {
                String line = inReader.readLine();
                if (line != null) {
                    mHandler.parseClientData(line);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                }

            } catch (Exception e) {
            }
        }
    }

    public void sendHello() {
        String data = "HELLO;";
        data += "VERSION:" + Constants.VERSION + Constants.VERSION_ADDITION + ";";
        data += "USER:" + sNick;//GlobalOptions.getProperty("account.name");
        data += "\n";
        sendOutput(data);
    }

    public void sendChat(String pText) {
        String data = "CHAT;";
        data += "USER:" + sNick + ";";//GlobalOptions.getProperty("account.name") + ";";
        data += "TEXT:\"" + pText + "\"";
        data += "\n";
        sendOutput(data);
    }

    public void sendBye() {
        String data = "BYE;";
        data += "\n";
        sendOutput(data);
    }

    public void sendOutput(String pData) {
        int attemps = 0;
        boolean error = false;
        while (attemps < 3) {
            try {
                outStream.write(pData);
                outStream.flush();
                //wait a while
                try {
                    Thread.sleep(100);
                } catch (Exception inner) {
                }
                error = false;
                break;
            } catch (Exception e) {
                error = true;
                attemps++;
                try {
                    Thread.sleep(100);
                } catch (Exception inner) {
                }
            }
        }
        if (error) {
            // logger.error("Failed to send data. Disconnecting client");
            disconnect();
        }
    }

    public void disconnect() {
        logger.debug("Disconnecting");
        sendBye();
        try {
            mClientSocket.close();
            inReader.close();
            outStream.close();
        } catch (Exception e) {
        }
        connected = false;
    }

    @Override
    public void fireServerHelloEvent(ComPeer pSource, String pNick, String pVersion) {
        //server event
    }

    @Override
    public void fireHelloEvent(String pUser) {
        fireHelloEvents(pUser);
    }

    @Override
    public void fireChatEvent(String pNick, String pText) {
        fireChatEvents(pNick, pText);
    }

    @Override
    public void fireByeEvent(String pUser) {
        fireByeEvents(pUser);
    }

    @Override
    public void fireServerByeEvent(ComPeer pSource) {
        //server event
    }

    @Override
    public void fireUserListEvent(String[] pUsers) {
        fireUserListEvents(pUsers);
    }

    @Override
    public void fireDisconnectEvent() {
        fireDisconnectEvents();
    }

    @Override
    public void fireKickEvent() {
        fireKickEvents();
    }

    @Override
    public void fireBanEvent() {
        fireBanEvents();
    }

    public void fireHelloEvents(String pUser) {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireHelloEvent(pUser);
        }
    }

    public void fireChatEvents(String pUser, String pText) {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireChatEvent(pUser, pText);
        }
    }

    public void fireUserListEvents(String[] pUsers) {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireUserListEvent(pUsers);
        }
    }

    public void fireByeEvents(String pUser) {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireByeEvent(pUser);
        }
    }

    public void fireDisconnectEvents() {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireDisconnectEvent();
        }
    }

    public void fireKickEvents() {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireKickEvent();
        }
    }

    public void fireBanEvents() {
        ComClientListener[] listeners = mListeners.toArray(new ComClientListener[]{});
        for (ComClientListener listener : listeners) {
            listener.fireBanEvent();
        }
    }
}
