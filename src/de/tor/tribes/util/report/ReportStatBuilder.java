/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.FightStats;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class ReportStatBuilder {

    public static FightStats buildStats(List<String> pReportSets) {
        FightStats stats = new FightStats();
        for (String set : pReportSets) {
            List<ManageableType> elements = ReportManager.getSingleton().getAllElements(set);
            for (ManageableType e : elements) {
                FightReport r = (FightReport) e;
                stats.includeReport(r);
            }
        }
        return stats;
    }
}
