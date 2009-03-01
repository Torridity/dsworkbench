/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class PeerManager implements ComServerListener, ComPeerListener, CommandHandlerListener {

    private static Logger logger = Logger.getLogger("ComManager");
    private List<ComPeer> mPeers = null;
    private ComServer mServer = null;
    private CommandHandler mHandler = null;
    private List<PeerManagerListener> mListeners = null;
    private List<String> banList = null;

    public PeerManager(int pServerPort) throws Exception {
        mPeers = new LinkedList<ComPeer>();
        mServer = new ComServer(pServerPort);
        mServer.setComServerListener(this);
        mHandler = new CommandHandler(this);
        mListeners = new LinkedList<PeerManagerListener>();
        banList = new LinkedList<String>();
        mServer.start();
    }

    public synchronized void addPeerManagerListener(PeerManagerListener pListener) {
        mListeners.add(pListener);
    }

    public synchronized void removePeerManagerListener(PeerManagerListener pListener) {
        mListeners.remove(pListener);
    }

    public synchronized void addPeer(ComPeer pPeer) {
        logger.debug("Adding new peer");
        pPeer.setComPeerListener(this);
        mPeers.add(pPeer);
    }

    public synchronized void removePeer(ComPeer pPeer) {
        logger.debug("Removing peer");
        mPeers.remove(pPeer);
    }

    public void addPeerToBanList(String pUser) {
        banList.add(pUser);
    }

    public void removePeerFromBanList(String pUser) {
        banList.remove(pUser);
    }

    public String[] getBans() {
        return banList.toArray(new String[]{});
    }

    public void kick(String pUser) {
        ComPeer[] peers = mPeers.toArray(new ComPeer[]{});
        for (ComPeer peer : peers) {
            if (peer.getUserName().equals(pUser)) {
                peer.kick();
                break;
            }
        }
    }

    public void ban(String pUser) {
        ComPeer[] peers = mPeers.toArray(new ComPeer[]{});
        for (ComPeer peer : peers) {
            if (peer.getUserName().equals(pUser)) {
                peer.ban();
                break;
            }
        }
        addPeerToBanList(pUser);
    }

    /**Global disconnect -> server was stopped
     */
    public void disconnect() {
        ComPeer[] peers = mPeers.toArray(new ComPeer[]{});
        for (ComPeer peer : peers) {
            peer.disconnect();
        }
        logger.info("Stopping server");
        mServer.stopServer();
    }

    @Override
    public void fireInputEvent(ComPeer pSource, String pData) {
        //parse server data
        if (pData.startsWith("HELLO")) {
            mHandler.parseServerData(pSource, pData);
            pData = "HELLO;USER:" + pSource.getUserName();
        } else if (pData.startsWith("BYE")) {
            mHandler.parseServerData(pSource, pData);
            pData = "BYE;USER:" + pSource.getUserName();
        }

        ComPeer[] peers = mPeers.toArray(new ComPeer[]{});
        //send data to all other peers
        for (ComPeer peer : peers) {
            if (!peer.equals(pSource)) {
                //add newline cause it was remove during read
                peer.sendOutput(pData + "\n");
            }
        }
    }

    public void sendUserList() {
        ComPeer[] peers = mPeers.toArray(new ComPeer[]{});
        String list = "USERLIST;";
        List<String> userList = new LinkedList<String>();
        for (ComPeer peer : peers) {
            String name = peer.getUserName();
            if (!name.equals("unknown")) {
                //user has hello'd already
                list += peer.getUserName() + ";";
                userList.add(name);
            }
        }
        //send data to all other peers
        for (ComPeer peer : peers) {
            //add newline cause it was remove during read
            peer.sendOutput(list + "\n");
        }
        fireUserListEvents(userList.toArray(new String[]{}));
    }

    public static void main(String[] args) throws Exception {
        PeerManager m = new PeerManager(6666);

        try {
            Thread.sleep(15000);
        } catch (Exception e) {
        }
        m.disconnect();
    }

    @Override
    public void firePeerDisconnectedEvent(ComPeer pSource) {
        System.out.println("Disconnect");
        fireServerByeEvent(pSource);
    }

    @Override
    public void fireClientConnectionEvent(ComPeer pClient) {
        addPeer(pClient);
    }

    @Override
    public void fireHelloEvent(String pUser) {
        //client event
    }

    @Override
    public void fireServerHelloEvent(ComPeer pSource, String pNick, String pVersion) {

        if (banList.contains(pNick)) {
            logger.info("Banned peer '" + pNick + "' tried to connect");
            pSource.kick();
            return;
        } else {
            logger.debug(pNick + " has connected");
        }
        pSource.setUserName(pNick);
        fireJoinEvents(pNick);
        sendUserList();
    }

    @Override
    public void fireChatEvent(String pNick, String pText) {
        //client event
    }

    @Override
    public void fireByeEvent(String pUser) {
        //client event
    }

    @Override
    public void fireDisconnectEvent() {
        //client event
    }

    @Override
    public void fireKickEvent() {
        //client event
    }

    @Override
    public void fireBanEvent() {
        //client event
    }

    @Override
    public void fireServerByeEvent(ComPeer pSource) {
        String name = pSource.getUserName();
        if (!name.equals("unknown")) {
            logger.debug(name + " has disconnected");
            fireLeaveEvents(name);
        }
        removePeer(pSource);
        sendUserList();
    }

    @Override
    public void fireUserListEvent(String[] pUsers) {
        //client event
    }

    public void fireJoinEvents(String pUser) {
        PeerManagerListener[] listeners = mListeners.toArray(new PeerManagerListener[]{});
        for (PeerManagerListener listener : listeners) {
            listener.fireJoinEvent(pUser);
        }
    }

    public void fireUserListEvents(String[] pUsers) {
        PeerManagerListener[] listeners = mListeners.toArray(new PeerManagerListener[]{});
        for (PeerManagerListener listener : listeners) {
            listener.fireUserListEvent(pUsers);
        }
    }

    public void fireLeaveEvents(String pUser) {
        PeerManagerListener[] listeners = mListeners.toArray(new PeerManagerListener[]{});
        for (PeerManagerListener listener : listeners) {
            listener.fireLeaveEvent(pUser);
        }
    }
}
