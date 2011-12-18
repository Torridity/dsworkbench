/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class TribeUtils {
    
    public static Tribe[] getTribeByVillage(Village pVillage, boolean pUseBarbarians, Comparator<Tribe> pComparator) {
        return getTribeByVillage(new Village[]{pVillage}, pUseBarbarians, pComparator);
    }

    public static Tribe[] getTribeByVillage(Village[] pVillages, boolean pUseBarbarians, Comparator<Tribe> pComparator) {
        List<Tribe> tribes = new LinkedList<Tribe>();
        
        for (Village v : pVillages) {
            Tribe t = v.getTribe();
            if (pUseBarbarians || !t.equals(Barbarians.getSingleton())) {
                if (!tribes.contains(t)) {
                    tribes.add(t);
                }
            }
        }
        
        if (pComparator != null) {
            Collections.sort(tribes, pComparator);
        }
        
        return tribes.toArray(new Tribe[tribes.size()]);
    }    
}
