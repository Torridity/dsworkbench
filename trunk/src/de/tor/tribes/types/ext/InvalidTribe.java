/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.ext;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.types.ext.NoAlly;

/**
 *
 * @author Torridity
 */
public class InvalidTribe extends Tribe {

    private static InvalidTribe SINGLETON = null;

    public static synchronized InvalidTribe getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new InvalidTribe();
        }
        return SINGLETON;
    }

    @Override
    public String getName() {
        return "gelöscht";
    }

    @Override
    public Village[] getVillageList() {
        return new Village[]{};
    }

    @Override
    public Ally getAlly() {
        return NoAlly.getSingleton();
    }
}
