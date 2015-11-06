/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.Attack;
import de.tor.tribes.types.FightReport;

/**
 *
 * @author Torridity
 */
public class ConqueredFilter implements ReportRuleInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return (c.wasConquered() || c.guessType() == Attack.SNOB_TYPE);
    }

    @Override
    public String getDescription() {
        return "Filtert Berichte mit AGs";
    }

    @Override
    public String getStringRepresentation() {
        return "AG-Angriffe/Eroberungen";
    }
}
