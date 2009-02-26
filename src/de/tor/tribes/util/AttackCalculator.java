/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Charon
 */
public class AttackCalculator {

    public static Hashtable<Village, Hashtable<Village, UnitHolder>> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            Date pStartTime,
            Date pArriveTime,
            int pMinTimeBetweenAttacks,
            int pTimeFrameStartHour,
            int pTimeFrameEndHour,
            boolean pNightBlock,
            boolean pRandomize) {

        List<Village> snobVillages = pSources.get(DataHolder.getSingleton().getUnitByPlainName("snob"));

        return null;
    }

    private static void buildSourceTargetMappings(Hashtable<UnitHolder, List<Village>> pSources, List<Village> pTargets) {
        Enumeration<UnitHolder> keys = pSources.keys();
        while (keys.hasMoreElements()) {
            UnitHolder unit = keys.nextElement();
            List<Village> sources = pSources.get(unit);
            for (Village source : sources) {
                for (Village target : pTargets) {
                    double dist = DSCalculator.calculateDistance(source, target);
                    
                }
            }

        }

    }
}
