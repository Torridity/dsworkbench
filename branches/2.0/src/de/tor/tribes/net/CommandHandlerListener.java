/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

/**
 *
 * @author Charon
 */
public interface CommandHandlerListener {

    public void fireServerHelloEvent(ComPeer pSource, String pNick, String pVersion);

    public void fireHelloEvent(String pNick);

    public void fireUserListEvent(String[] pUsers);

    public void fireChatEvent(String pNick, String pText);

    public void fireServerByeEvent(ComPeer pSource);

    public void fireByeEvent(String pNick);

    public void fireDisconnectEvent();

    public void fireKickEvent();

    public void fireBanEvent();
}
