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
public class ColorFilter implements ReportFilterInterface {

    private Integer color = 31;
    public static final int GREY = 1;
    public static final int BLUE = 2;
    public static final int RED = 4;
    public static final int YELLOW = 8;
    public static final int GREEN = 16;

    @Override
    public void setup(Object pFilterComponent) {
        color = (Integer) pFilterComponent;
    }

    @Override
    public boolean isValid(FightReport c) {
        int value = 0;
        if (c.areAttackersHidden()) {
            value = GREY;
        } else if (c.isSpyReport()) {
            value = BLUE;
        } else if (c.wasLostEverything()) {
            value = RED;
        } else if (c.wasLostNothing()) {
            value = GREEN;
        } else {
            value = YELLOW;
        }
        return ((color & value) > 0);
    }

    public static void main(String[] args) {
        ColorFilter f = new ColorFilter();
        f.setup(ColorFilter.RED + ColorFilter.BLUE + ColorFilter.GREEN);

    }
}
