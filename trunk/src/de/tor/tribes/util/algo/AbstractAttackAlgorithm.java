/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.AbstractTroopMovement;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.algo.AlgorithmLogPanel;
import de.tor.tribes.util.ServerSettings;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @TOTO (DIFF) Check max. snob runtime
 * @author Jejkal
 */
public abstract class AbstractAttackAlgorithm extends Thread {

    private static Logger logger = Logger.getLogger("AttackAlgorithm");
    private List<AbstractTroopMovement> results = null;
    private Hashtable<UnitHolder, List<Village>> sources = null;
    private Hashtable<UnitHolder, List<Village>> fakes = null;
    private List<Village> targets = null;
    private List<Village> fakeTargets = null;
    private Hashtable<Village, Integer> maxAttacksTable;
    private TimeFrame timeFrame = null;
    boolean fakeOffTargets = false;
    private AlgorithmListener mListener = null;
    private AlgorithmLogPanel mLogPanel = null;

    public void initialize(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakedTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets,
            AlgorithmLogPanel pLogPanel) {
        sources = pSources;
        fakes = pFakes;
        targets = pTargets;
        fakeTargets = pFakedTargets;
        maxAttacksTable = pMaxAttacksTable;
        timeFrame = pTimeFrame;
        fakeOffTargets = pFakeOffTargets;
        mLogPanel = pLogPanel;
    }

    public abstract List<AbstractTroopMovement> calculateAttacks(
            Hashtable<UnitHolder, List<Village>> pSources,
            Hashtable<UnitHolder, List<Village>> pFakes,
            List<Village> pTargets,
            List<Village> pFakeTargets,
            Hashtable<Village, Integer> pMaxAttacksTable,
            TimeFrame pTimeFrame,
            boolean pFakeOffTargets);

    public void logText(String pText) {
        if (mLogPanel != null) {
            mLogPanel.addText(pText);
        }
    }

    public void logInfo(String pText) {
        if (mLogPanel != null) {
            mLogPanel.addInfo(pText);
        }
    }

    public void logError(String pText) {
        if (mLogPanel != null) {
            mLogPanel.addError(pText);
        }
    }

    public void updateStatus(int pCurrentStatus, int pMaxStatus) {
        if (mLogPanel != null) {
            mLogPanel.updateStatus(pCurrentStatus, pMaxStatus);
        }
    }

    public boolean isAborted() {
        if (mLogPanel != null) {
            return mLogPanel.isAborted();
        }
        return false;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public List<AbstractTroopMovement> getResults() {
        return results;
    }

    public void execute(AlgorithmListener pListener) {
        mListener = pListener;
        start();
    }

    /**
     * @return the fullOffs
     */
    public int getFullOffs() {
        int cnt = 0;
        for (AbstractTroopMovement movement : results) {
            cnt += (movement.offComplete()) ? 1 : 0;
        }
        return cnt;
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

    @Override
    public void run() {
        try {
            results = calculateAttacks(sources, fakes, targets, fakeTargets, maxAttacksTable, timeFrame, fakeOffTargets);
        } catch (Exception e) {
            //an error occured
            logger.error("An error occured during calculation", e);
            e.printStackTrace();
            results = new LinkedList<AbstractTroopMovement>();
        }
        mListener.fireCalculationFinishedEvent(this);
    }
}
