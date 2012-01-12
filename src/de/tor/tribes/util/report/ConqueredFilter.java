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
public class ConqueredFilter implements ReportFilterInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return !(c.wasConquered() || c.wasSnobAttack());
    }

    @Override
    public String getDescription() {
        return "Filtert Adelungen";
    }

    @Override
    public String getStringRepresentation() {
        return "Adelungen";
    }
}
