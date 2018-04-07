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
import de.tor.tribes.types.TroopMovement;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.algo.AlgorithmLogPanel;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.algo.types.DistanceMapping;
import de.tor.tribes.util.algo.types.TimeFrame;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Torridity
 */
public abstract class AbstractAttackAlgorithm extends Thread {
    
    private static Logger logger = Logger.getLogger("AttackAlgorithm");
    private List<TroopMovement> results = null;
    private Hashtable<UnitHolder, List<Village>> sources = null;
    private Hashtable<UnitHolder, List<Village>> fakes = null;
    private List<Village> targets = null;
    private List<Village> fakeTargets = null;
    private Hashtable<Village, Integer> maxAttacksTable;
    private TimeFrame timeFrame = null;
    boolean fakeOffTargets = false;
    private AlgorithmListener mListener = null;
    private AlgorithmLogPanel mLogPanel = null;
    private boolean running = false;
    private boolean aborted = false;
    private LogListener listener = null;
  
    public AbstractAttackAlgorithm() {
        setName("AttackAlgorithmCalculationThread");
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }
    
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
    
    public void setLogListener(LogListener pListener) {
        listener = pListener;
    }
    
    public LogListener getLogListener() {
        return listener;
    }
    
    public abstract List<TroopMovement> calculateAttacks(
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
        if (listener != null) {
            listener.logMessage(pText);
        }
    }
    
    public void logInfo(String pText) {
        if (mLogPanel != null) {
            mLogPanel.addInfo(pText);
        }
        if (listener != null) {
            listener.logMessage(pText);
        }
    }
    
    public void logError(String pText) {
        if (mLogPanel != null) {
            mLogPanel.addError(pText);
        }
        
        if (listener != null) {
            listener.logMessage(pText);
        }
    }
    
    public void updateStatus(int pCurrentStatus, int pMaxStatus) {
        if (mLogPanel != null) {
            mLogPanel.updateStatus(pCurrentStatus, pMaxStatus);
        }
        
        if (listener != null) {
            listener.updateProgress(100.0 * (double) pCurrentStatus / (double) pMaxStatus);
        }
        
    }
    
    public boolean isAborted() {
        return aborted;
    }
    
    public void abort() {
        aborted = true;
    }
    
    public TimeFrame getTimeFrame() {
        return timeFrame;
    }
    
    public List<TroopMovement> getResults() {
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
        for (TroopMovement movement : results) {
            cnt += (movement.offComplete()) ? 1 : 0;
        }
        return cnt;
    }
    
    public static List<DistanceMapping> buildSourceTargetsMapping(Village pSource, List<Village> pTargets, boolean pIsSnob) {
        List<DistanceMapping> mappings = new LinkedList<>();
        
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
        running = true;
        try {
            results = calculateAttacks(sources, fakes, targets, fakeTargets, maxAttacksTable, timeFrame, fakeOffTargets);
        } catch (Exception e) {
            //an error occured
            logger.error("An error occured during calculation", e);
            results = new LinkedList<>();
        }
        running = false;
        if (listener != null) {
            listener.calculationFinished();
        }
        if (mListener != null) {
            mListener.fireCalculationFinishedEvent(this);
        }
    }
    
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public interface LogListener {
        
        void logMessage(String pMessage);
        
        void updateProgress(double pPercent);
        
        void calculationFinished();
    }
}
