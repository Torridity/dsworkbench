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
public class CommandHandler {

    private static Logger logger = Logger.getLogger("Client-Parser");
    public final int ID_HELLO = 0;
    public final int ID_CHAT = 1;
    public final int ID_BYE = 666;
    private CommandHandlerListener mCommandHandlerListener = null;

    public CommandHandler(CommandHandlerListener pListener) {
        mCommandHandlerListener = pListener;
    }

    public void parseClientData(String pData) {
        if (pData.startsWith("HELLO")) {
            parseHello(pData);
        } else if (pData.startsWith("BYE")) {
            parseBye(pData);
        } else if (pData.startsWith("CHAT")) {
            parseChat(pData);
        } else if (pData.startsWith("USERLIST")) {
            parseUserList(pData);
        } else if (pData.startsWith("DISCONNECT")) {
            parseDisconnect();
        } else if (pData.startsWith("KICK")) {
            parseKick();
        } else if (pData.startsWith("BAN")) {
            parseBan();
        }
    }

    public void parseServerData(ComPeer pSource, String pData) {
        if (pData.startsWith("HELLO")) {
            parseServerHello(pSource, pData);
        } else if (pData.startsWith("BYE")) {
            parseServerBye(pSource);
        }
    }

    private void parseServerHello(ComPeer pSource, String pData) {
        String[] split = pData.split(";");
        String version = split[1];
        version = version.replaceFirst("VERSION:", "");
        String user = split[2];
        user = user.replaceFirst("USER:", "");
        mCommandHandlerListener.fireServerHelloEvent(pSource, user, version);
    }

    private void parseHello(String pData) {
        String[] split = pData.split(";");
        String user = split[1];
        user = user.replaceFirst("USER:", "");
        mCommandHandlerListener.fireHelloEvent(user);
    }

    private void parseChat(String pData) {
        String[] split = pData.split(";");
        String user = split[1].replaceAll("USER:", "");
        String text = split[2].replaceAll("TEXT:", "");
        text = text.replaceAll("\"", "");
        mCommandHandlerListener.fireChatEvent(user, text);
    }

    private void parseUserList(String pData) {
        String[] split = pData.split(";");
        List<String> users = new LinkedList<String>();
        for (int i = 1; i < split.length; i++) {
            try {
                String user = split[i];
                if (user.length() > 3) {
                    users.add(user);
                }
            } catch (Exception e) {
            }
        }
        mCommandHandlerListener.fireUserListEvent(users.toArray(new String[]{}));
    }

    private void parseServerBye(ComPeer pSource) {
        mCommandHandlerListener.fireServerByeEvent(pSource);
    }

    private void parseDisconnect() {
        mCommandHandlerListener.fireDisconnectEvent();
    }

    private void parseKick() {
        mCommandHandlerListener.fireKickEvent();
    }

    private void parseBan() {
        mCommandHandlerListener.fireBanEvent();
    }

    private void parseBye(String pData) {
        String[] split = pData.split(";");
        String user = split[1];
        user = user.replaceFirst("USER:", "");
        mCommandHandlerListener.fireByeEvent(user);
    }
}
