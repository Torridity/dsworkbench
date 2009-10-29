/*
 * DataHolderListener.java
 * 
 * Created on 06.09.2007, 18:03:40
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.tor.tribes.io;

/**
 *
 * @author Charon
 */
public interface DataHolderListener {

    public void fireDataHolderEvent(String pFile);    
    public void fireDataLoadedEvent(boolean pSuccess);    
}
