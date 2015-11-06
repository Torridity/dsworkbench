/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class UnknownUnit extends UnitHolder {

    private static UnknownUnit SINGLETON = null;

    public static synchronized UnitHolder getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new UnknownUnit();
        }
        return SINGLETON;
    }

    UnknownUnit() {
        setName("unbekannt");
        setPlainName("unknown");
    }
}
