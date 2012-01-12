/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class AllyFilter implements ReportFilterInterface {

    private List<String> allies = null;

    @Override
    public void setup(Object pFilterComponent) {
        String[] allySplit = ((String) pFilterComponent).split(";");
        allies = new LinkedList<String>();
        for (String split : allySplit) {
            if (split != null) {
                Ally a = DataHolder.getSingleton().getAllyByName(split.trim());
                if (a != null) {
                    allies.add(split.trim());
                }
            }
        }
    }

    @Override
    public boolean isValid(FightReport c) {
        if (c == null) {
            return false;
        }
        Ally a = (c.getAttacker() != null) ? c.getAttacker().getAlly() : NoAlly.getSingleton();
        Ally d = (c.getDefender() != null) ? c.getDefender().getAlly() : NoAlly.getSingleton();
        return (allies.contains(a.getTag()) || allies.contains(d.getTag()));
    }

    @Override
    public String getDescription() {
        return "Filterung nach Stammestags (Angreifer oder Verteidiger).";
    }

    @Override
    public String getStringRepresentation() {
        StringBuilder result = new StringBuilder();
        result.append("Stammestags ").append(allies.toString());
        return result.toString();
    }
}
