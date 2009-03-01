/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.net;

/**
 *
 * @author Charon
 */
public interface ComServerListener {

    public void fireClientConnectionEvent(ComPeer pClient);
}
