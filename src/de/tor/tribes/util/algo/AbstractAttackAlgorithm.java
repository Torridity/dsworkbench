/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Village;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public abstract class AbstractAttackAlgorithm {

    private int validEnoblements = 0;
    private int fullOffs = 0;

    public abstract List<AbstractTroopMovement> calculateAttacks(
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
            boolean pRandomize);

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
}
