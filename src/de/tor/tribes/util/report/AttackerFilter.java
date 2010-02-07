/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.Tribe;

/**
 *
 * @author Torridity
 */
public class AttackerFilter implements ReportFilterInterface {

    private Tribe owner = null;

    @Override
    public void setup(Object pFilterComponent) {
        owner = (Tribe) pFilterComponent;
    }

    @Override
    public boolean isValid(FightReport c) {
        try {
            return c.getAttacker().equals(owner);
        } catch (Exception e) {
            return false;
        }
    }
}
