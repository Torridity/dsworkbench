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
import de.tor.tribes.ui.views.DSWorkbenchSOSRequestAnalyzer;
import de.tor.tribes.ui.wiz.dep.DefenseAnalysePanel;
import de.tor.tribes.ui.wiz.dep.DefenseCalculationSettingsPanel;
import de.tor.tribes.ui.wiz.dep.DefenseFilterPanel;
import de.tor.tribes.ui.wiz.dep.types.SupportSourceElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DefenseCalculator extends Thread {

    private boolean isRunning = false;
    private boolean aborted = false;
    private boolean multiSupport = false;

    public DefenseCalculator() {
        setName("DefenseCalculator");
        setDaemon(true);
        setName("DefenseCalculationThread");
        setPriority(MIN_PRIORITY);
    }

    public void setAllowMultiSupport(boolean pValue) {
        multiSupport = pValue;
    }

    @Override
    public void run() {
        isRunning = true;
        DefenseInformation[] defenses = DefenseAnalysePanel.getSingleton().getAllElements();
        HashMap<de.tor.tribes.types.ext.Village, Integer> splits = new HashMap<>();
        List<de.tor.tribes.types.ext.Village> usedVillages = new LinkedList<>();

        for (SupportSourceElement element : DefenseFilterPanel.getSingleton().getFilteredElements()) {
            splits.put(element.getVillage(), element.getSupports());
            usedVillages.add(element.getVillage());
        }

        UnitHolder unit = DSWorkbenchSOSRequestAnalyzer.getSingleton().getSlowestUnit();
        for(de.tor.tribes.types.ext.Village v: splits.keySet()) {
            if (!usedVillages.contains(v)) {
                splits.remove(v);
            }
        }

        DefenseBruteForce algo = new DefenseBruteForce(multiSupport);
        algo.calculateDefenses(splits, defenses, unit, this);
        isRunning = false;
        DefenseCalculationSettingsPanel.getSingleton().notifyCalculationFinished();
    }

    public void abort() {
        aborted = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void logMessage(String pMessage) {
        DefenseCalculationSettingsPanel.getSingleton().notifyStatusUpdate(pMessage);
    }
}
