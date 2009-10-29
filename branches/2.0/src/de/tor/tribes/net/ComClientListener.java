/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

/**
 *
 * @author Charon
 */
public interface ComClientListener {

    public void fireHelloEvent(String pUser);

    public void fireChatEvent(String pUser, String pText);

    public void fireUserListEvent(String[] pUsers);

    public void fireByeEvent(String pUser);

    public void fireDisconnectEvent();

    public void fireKickEvent();

    public void fireBanEvent();
}
