/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.ext.Village;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
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

    public void calculateDefenses(
            Hashtable<Village, Integer> pDefenseSources,
            DefenseInformation[] pDefenseRequirements,
            UnitHolder pUnit,
            DefenseCalculator pParent) {

        Enumeration<Village> sourceKeys = pDefenseSources.keys();

        List<DefenseInformation> defenses = Arrays.asList(pDefenseRequirements);

        logger.debug("Assigning defenses");
        while (sourceKeys.hasMoreElements()) {
            Village source = sourceKeys.nextElement();
            pParent.logMessage("Suche mögliche Ziele für Dorf " + source);
            int availableSupports = pDefenseSources.get(source);
            //do good old brute force shuffle
            Collections.shuffle(defenses);
            for (DefenseInformation defense : defenses) {
                if (defense.isSave()) {
                    pParent.logMessage("Unterstützungen für Ziel " + defense.getTarget() + " vollständig");
                    //finished with this target
                    break;
                } else if (defense.getNeededSupports() == defense.getSupports().length) {
                    pParent.logMessage(defense.getTarget() + " benötigt keine Unterstützungen");
                    //finished with this target
                    break;
                }

                //assign primary supports only
                if (defense.addSupport(source, pUnit, true, false)) {
                    pParent.logMessage("Füge primäre Unterstützung von " + source + " nach " + defense.getTarget() + " hinzu");
                    availableSupports--;
                } else {
                    pParent.logMessage("Primäre Unterstützung von " + source + " nach " + defense.getTarget() + " nicht möglich");
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
                for (DefenseInformation defense : defenses) {
                    if (defense.isSave()) {
                        //finished with this target
                        pParent.logMessage("Unterstützungen für Ziel " + defense.getTarget() + " vollständig");
                        break;
                    }

                    //assign secondary supports
                    if (defense.addSupport(source, pUnit, false, false)) {
                        pParent.logMessage("Füge sekundäre Unterstützung von " + source + " nach " + defense.getTarget() + " hinzu");
                        availableSupports--;
                    } else {
                        pParent.logMessage("Sekundäre Unterstützung von " + source + " nach " + defense.getTarget() + " nicht möglich");
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
                for (DefenseInformation defense : defenses) {
                    if (defense.isSave()) {
                        //finished with this target
                        pParent.logMessage("Unterstützungen für Ziel " + defense.getTarget() + " vollständig");
                        break;
                    }

                    //assign secondary supports
                    if (defense.addSupport(source, pUnit, false, true)) {
                        pParent.logMessage("Füge Mehrfachunterstützung von " + source + " nach " + defense.getTarget() + " hinzu");
                        availableSupports--;
                    } else {
                        pParent.logMessage("Mehrfachunterstützung von " + source + " nach " + defense.getTarget() + " nicht möglich");
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
    }
}
