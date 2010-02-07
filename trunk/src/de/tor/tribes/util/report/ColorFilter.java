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
public class ColorFilter implements ReportFilterInterface {

    private Integer color = GREY;
    public static final int GREY = 0;
    public static final int BLUE = 1;
    public static final int RED = 2;
    public static final int YELLOW = 3;
    public static final int GREEN = 4;

    @Override
    public void setup(Object pFilterComponent) {
        color = (Integer) pFilterComponent;
    }

    @Override
    public boolean isValid(FightReport c) {
        if (c.areAttackersHidden() && color == GREY) {
            return true;
        } else if (c.isSpyReport() && color == BLUE) {
            return true;
        } else if (c.wasLostEverything() && color == RED) {
            return true;
        } else if (c.wasLostNothing() && color == GREEN) {
            return true;
        } else if (color == YELLOW) {
            return true;
        }
        return false;
    }
}
