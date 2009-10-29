/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

/**
 *
 * @author Charon
 */
public interface PeerManagerListener {

    public void fireJoinEvent(String pUser);

    public void fireLeaveEvent(String pUser);

    public void fireUserListEvent(String[] pUsers);
}
