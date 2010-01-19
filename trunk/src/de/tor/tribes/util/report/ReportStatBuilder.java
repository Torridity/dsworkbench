/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.FightStats;
import de.tor.tribes.types.ReportSet;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class ReportStatBuilder {

    public static FightStats buildStats(List<String> pReportSets) {
        FightStats stats = new FightStats();
        for (String set : pReportSets) {
            ReportSet rSet = ReportManager.getSingleton().getReportSet(set);
            for (FightReport r : rSet.getReports()) {
                stats.includeReport(r);
            }
        }
        return stats;
    }
}
