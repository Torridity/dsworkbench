/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Defense;
import de.tor.tribes.types.DefenseElement;
import de.tor.tribes.types.Village;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class DefenseBruteForce {

    private static Logger logger = Logger.getLogger("Algorithm_DefenseBruteForce");
    private boolean allowMultiSupport = false;

    public DefenseBruteForce(boolean pMultiSupport) {
        allowMultiSupport = pMultiSupport;
    }

    public List<Defense> calculateAttacks(
            Hashtable<Village, Integer> pDefenseSources,
            DefenseElement[] pDefenseRequirements,
            UnitHolder pUnit,
            DefenseCalculator pParent) {

        Enumeration<Village> sourceKeys = pDefenseSources.keys();
        List<Defense> defenses = new LinkedList<Defense>();
        for (DefenseElement element : pDefenseRequirements) {
            defenses.add(new Defense(element, pUnit));
        }
        logger.debug("Assigning defenses");
        while (sourceKeys.hasMoreElements()) {
            Village source = sourceKeys.nextElement();
            pParent.logMessage("Suche mögliche Ziele für Dorf " + source);
            int availableSupports = pDefenseSources.get(source);
            //do good old brute force shuffle
            Collections.shuffle(defenses);
            for (Defense defense : defenses) {
                if (defense.isFinished()) {
                    pParent.logMessage("Unterstützungen für Ziel " + defense.getTarget() + " vollständig");
                    //finished with this target
                    break;
                }

                //assign primary supports only
                if (defense.addSupport(source, true, false)) {
                    pParent.logMessage("Füge primäre Unterstützung von " + source + " nach " + defense.getTarget() + " hinzu");
                    availableSupports--;
                }

                if (availableSupports == 0) {
                    pParent.logMessage("Keine weiteren Unterstützungen aus " + source + " verfügbar");
                    //no more defenses from this source
                    pDefenseSources.put(source, 0);
                    break;
                }
            }

            if (availableSupports != 0) {//assign secondary supports
                //...and shuffle before, of course
                Collections.shuffle(defenses);
                for (Defense defense : defenses) {
                    if (defense.isFinished()) {
                        //finished with this target
                        pParent.logMessage("Unterstützungen für Ziel " + defense.getTarget() + " vollständig");
                        break;
                    }

                    //assign secondary supports
                    if (defense.addSupport(source, false, false)) {
                        pParent.logMessage("Füge sekundäre Unterstützung von " + source + " nach " + defense.getTarget() + " hinzu");
                        availableSupports--;
                    }

                    if (availableSupports == 0) {
                        pParent.logMessage("Keine weiteren Unterstützungen aus " + source + " verfügbar");
                        //no more defenses from this source
                        pDefenseSources.put(source, 0);
                        break;
                    }
                }
            }

            if (availableSupports != 0 && allowMultiSupport) {//do again with multi support
                //assign secondary supports...and shuffle before, of course
                Collections.shuffle(defenses);
                for (Defense defense : defenses) {
                    if (defense.isFinished()) {
                        //finished with this target
                        pParent.logMessage("Unterstützungen für Ziel " + defense.getTarget() + " vollständig");
                        break;
                    }

                    //assign secondary supports
                    if (defense.addSupport(source, false, true)) {
                        pParent.logMessage("Füge sekundäre Unterstützung von " + source + " nach " + defense.getTarget() + " hinzu");
                        availableSupports--;
                    }

                    if (availableSupports == 0) {
                        pParent.logMessage("Keine weiteren Unterstützungen aus " + source + " verfügbar");
                        //no more defenses from this source
                        pDefenseSources.put(source, 0);
                        break;
                    }
                }
            }

            //update remaining
            pDefenseSources.put(source, availableSupports);
            if (availableSupports > 0) {
                pParent.logMessage(availableSupports + " Unterstützung(en) aus " + source + " konnte(n) nicht verwendet werden");
            }
        }

        //convert to result list
        pParent.logMessage("Berechnung abgeschlossen");
        return defenses;
    }
}
