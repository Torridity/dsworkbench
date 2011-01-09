/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;

/**
 *
 * @author Torridity
 */
public class ImpossibleSnobUnit extends UnitHolder {

    private static ImpossibleSnobUnit SINGLETON = null;

    public static synchronized ImpossibleSnobUnit getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ImpossibleSnobUnit();
        }
        return SINGLETON;
    }

    ImpossibleSnobUnit() {
        setName("Adelsgeschlecht");
        setPlainName("snob");
    }
}
