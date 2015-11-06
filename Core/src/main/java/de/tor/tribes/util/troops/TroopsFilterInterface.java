/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.types.ext.Village;

/**
 *
 * @author Charon
 */
public interface TroopsFilterInterface {

    public void setup(Object pFilterComponent);

    public boolean isValid(Village pVillage);
}
