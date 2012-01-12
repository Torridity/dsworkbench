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
public class OffFilter implements ReportFilterInterface {

    @Override
    public void setup(Object pFilterComponent) {
    }

    @Override
    public boolean isValid(FightReport c) {
        return (c.guessType() == Attack.CLEAN_TYPE);
    }

    @Override
    public String getDescription() {
        return "Filtert Off-Berichte";
    }

    @Override
    public String getStringRepresentation() {
        return "Off-Berichte";
    }
}