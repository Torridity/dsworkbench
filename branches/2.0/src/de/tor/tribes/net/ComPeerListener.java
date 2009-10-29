/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

/**
 *
 * @author Charon
 */
public interface ComPeerListener {

    public void fireInputEvent(ComPeer pSource, String pData);

    public void firePeerDisconnectedEvent(ComPeer pSource);
}
