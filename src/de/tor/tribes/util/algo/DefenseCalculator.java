/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Defense;
import de.tor.tribes.types.DefenseElement;
import de.tor.tribes.ui.wiz.dep.AnalysePanel;
import de.tor.tribes.ui.wiz.dep.FilterPanel;
import de.tor.tribes.ui.wiz.dep.FinalSettingsPanel;
import de.tor.tribes.ui.wiz.dep.VillagePanel;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DefenseCalculator extends Thread {

    private boolean isRunning = false;
    private boolean aborted = false;
    private List<Defense> results = null;
    private boolean multiSupport = false;

    public DefenseCalculator() {
        setDaemon(true);
    }

    public void setAllowMultiSupport(boolean pValue) {
        multiSupport = pValue;
    }

    @Override
    public void run() {
        results = null;
        isRunning = true;
        DefenseElement[] defenses = AnalysePanel.getSingleton().getModel().getRows();
        Hashtable<de.tor.tribes.types.Village, Integer> splits = VillagePanel.getSingleton().getSplits();
        List<de.tor.tribes.types.Village> usedVillages = Arrays.asList(FilterPanel.getSingleton().getFilteredVillages());
        UnitHolder unit = AnalysePanel.getSingleton().getSlowestUnit();
        Enumeration<de.tor.tribes.types.Village> villageKeys = splits.keys();
        while (villageKeys.hasMoreElements()) {
            de.tor.tribes.types.Village v = villageKeys.nextElement();
            if (!usedVillages.contains(v)) {
                splits.remove(v);
            }
        }

        DefenseBruteForce algo = new DefenseBruteForce(multiSupport);
        results = algo.calculateAttacks(splits, defenses, unit, this);
        isRunning = false;
        FinalSettingsPanel.getSingleton().notifyCalculationFinished();
    }

    public Defense[] getResults() {
        if (hasResults()) {
            return results.toArray(new Defense[results.size()]);
        } else {
            return new Defense[0];
        }
    }

    public boolean hasResults() {
        return results != null && !results.isEmpty();
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
        FinalSettingsPanel.getSingleton().notifyStatusUpdate(pMessage);
    }
}
