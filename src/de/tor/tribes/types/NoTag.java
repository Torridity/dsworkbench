/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

/**
 *
 * @author Charon
 */
public class NoTag extends Tag {

    private static NoTag SINGLETON = null;

    public static synchronized NoTag getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoTag("Kein Tag", false);
        }
        return SINGLETON;
    }

    NoTag(String pName, boolean pShowOnMap) {
        super(pName, pShowOnMap);
    }

    public boolean tagsVillage(int pVillageID) {
        return false;
    }
}
