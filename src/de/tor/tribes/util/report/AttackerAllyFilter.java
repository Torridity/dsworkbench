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
public class AttackerAllyFilter implements ReportFilterInterface {

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
        if (c == null || allies.isEmpty()) {
            return false;
        }
        Ally a = (c.getAttacker() != null) ? c.getAttacker().getAlly() : NoAlly.getSingleton();
        return allies.contains(a.getTag());
    }

    @Override
    public String getDescription() {
        return "Filterung nach Stammestags der Angreifers";
    }

    @Override
    public String getStringRepresentation() {
        StringBuilder result = new StringBuilder();
        result.append("Angreifende Stämme ").append(allies.toString());
        return result.toString();
    }
}
