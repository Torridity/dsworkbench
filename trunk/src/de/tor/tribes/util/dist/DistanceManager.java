/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.dist;

import de.tor.tribes.types.ext.Village;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class DistanceManager {

    private List<Village> villages = null;
    private static DistanceManager SINGLETON = null;

    public static synchronized DistanceManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DistanceManager();
        }
        return SINGLETON;
    }

    DistanceManager() {
        villages = new LinkedList<Village>();
    }

    public void clear() {
        villages.clear();
    }

    public Village[] getVillages() {
        return villages.toArray(new Village[]{});
    }

    public void removeVillages(int[] pIds) {
        List<Village> tmp = new LinkedList<Village>();

        for (int col : pIds) {
            int tCol = col - 1;
            if (tCol >= 0) {
                tmp.add(villages.get(tCol));
            }
        }

        for (Village v : tmp) {
            villages.remove(v);
        }
    }

    public void addVillage(Village pVillage) {
        if (!villages.contains(pVillage)) {
            villages.add(pVillage);
        }
    }
}
