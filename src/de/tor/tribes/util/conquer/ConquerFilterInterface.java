/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.types.Conquer;

/**
 *
 * @author Charon
 */
public interface ConquerFilterInterface {

    public abstract void setup(Object pFilterComponent);

    public boolean isValid(Conquer pConquer);
}
