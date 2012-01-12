/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Tribe;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DefenderFilter implements ReportFilterInterface {

    private List<String> tribes = null;

    @Override
    public void setup(Object pFilterComponent) {
        String[] tribeSplit = ((String) pFilterComponent).split(";");
        tribes = new LinkedList<String>();
        for (String split : tribeSplit) {
            if (split != null) {
                Tribe t = DataHolder.getSingleton().getTribeByName(split.trim());
                if (t != null) {
                    tribes.add(split.trim());
                }
            }
        }
    }

    @Override
    public boolean isValid(FightReport c) {
        try {
            for (String t : tribes) {
                if (t.equals(c.getDefender().getName())) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Filterung nach Angreifer.";
    }

    @Override
    public String getStringRepresentation() {
        StringBuilder result = new StringBuilder();
        result.append("Verteidiger ").append(tribes.toString());
        return result.toString();
    }
}
