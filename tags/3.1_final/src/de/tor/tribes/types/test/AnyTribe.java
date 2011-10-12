/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.Tribe;

/**
 *
 * @author Torridity
 */
public class AnyTribe extends Tribe {

    private static AnyTribe SINGLETON = null;

    public static synchronized AnyTribe getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new AnyTribe();
        }
        return SINGLETON;
    }

    @Override
    public String getName() {
        return "Jeder";
    }
}
