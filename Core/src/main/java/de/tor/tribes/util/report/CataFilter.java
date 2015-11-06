/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;

/**
 *
 * @author Torridity
 */
public class CataFilter implements ReportRuleInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return c.wasBuildingDamaged();
    }

    @Override
    public String getDescription() {
        return "Filtert Berichte mit Gebäudebeschädigung";
    }

    @Override
    public String getStringRepresentation() {
        return "Berichte mit Gebäudebeschädigung";
    }
}