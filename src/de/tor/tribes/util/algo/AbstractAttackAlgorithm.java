/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.ServerSettings;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @TOTO (DIFF) Check max. snob runtime
 * @author Jejkal
 */
public abstract class AbstractAttackAlgorithm {

    private int validEnoblements = 0;
    private int fullOffs = 0;

    public abstract List<Village> getNotAssignedSources();

    public abstract List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            int pMaxAttacksPerVillage,
            int pMaxCleanPerSnob,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets,
            boolean pUse5Snobs);

    /**
     * @return the validEnoblements
     */
    public int getValidEnoblements() {
        return validEnoblements;
    }

    /**
     * @param validEnoblements the validEnoblements to set
     */
    public void setValidEnoblements(int validEnoblements) {
        this.validEnoblements = validEnoblements;
    }

    /**
     * @return the fullOffs
     */
    public int getFullOffs() {
        return fullOffs;
    }

    /**
     * @param fullOffs the fullOffs to set
     */
    public void setFullOffs(int fullOffs) {
        this.fullOffs = fullOffs;
    }

    public static List<DistanceMapping> buildSourceTargetsMapping(Village pSource, List<Village> pTargets, boolean pIsSnob) {
        List<DistanceMapping> mappings = new LinkedList<DistanceMapping>();

        for (Village target : pTargets) {
            DistanceMapping mapping = new DistanceMapping(pSource, target);
            if (pIsSnob) {
                if (mapping.getDistance() < ServerSettings.getSingleton().getSnobRange()) {
                    //do not add snob distance if it is too large
                    mappings.add(mapping);
                }
            } else {
                mappings.add(mapping);
            }
        }

        Collections.sort(mappings);
        return mappings;
    }
}
