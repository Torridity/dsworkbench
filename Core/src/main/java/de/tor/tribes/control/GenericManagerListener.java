/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.control;

/**
 *
 * @author Torridity
 */
public interface GenericManagerListener {

    public void dataChangedEvent();

    public void dataChangedEvent(String pGroup);
}
