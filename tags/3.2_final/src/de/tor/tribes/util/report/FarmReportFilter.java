/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;
import de.tor.tribes.util.farm.FarmManager;

/**
 *
 * @author jejkal
 */
public class FarmReportFilter implements ReportRuleInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return c != null && FarmManager.getSingleton().getFarmInformation(c.getTargetVillage()) != null;
    }

    @Override
    public String getDescription() {
        return "Filterung von Farmberichten";
    }

    @Override
    public String getStringRepresentation() {
        return "Farmberichte";
    }
}
