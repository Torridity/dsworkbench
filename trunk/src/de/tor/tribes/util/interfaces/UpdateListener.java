/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.interfaces;

/**
 *
 * @author Torridity
 */
public interface UpdateListener {

    void fireResourceUpdatedEvent(String pResource, double pPercentFinished);

    void fireUpdateFinishedEvent(boolean pSuccess, String pMessage);
    
}
