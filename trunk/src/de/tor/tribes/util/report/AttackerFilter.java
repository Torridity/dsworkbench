/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class AttackerFilter implements ReportRuleInterface {

    private List<String> tribes = null;

    @Override
    public void setup(Object pFilterComponent) throws ReportRuleConfigurationException {

        try {
            String[] tribeSplit = ((String) pFilterComponent).split(";");
            if (tribeSplit == null || tribeSplit.length == 0) {
                throw new ReportRuleConfigurationException("Kein Spielername gefunden");
            }
            tribes = new LinkedList<String>();
            for (String split : tribeSplit) {
                if (split != null) {
                    Tribe t = DataHolder.getSingleton().getTribeByName(split.trim());
                    if (t != null && !t.equals(InvalidTribe.getSingleton())) {
                        tribes.add(split.trim());
                    }
                }
            }
            if (tribes.isEmpty()) {
                throw new ReportRuleConfigurationException("Kein Spielername gefunden");
            }
        } catch (Throwable t) {
            if (t instanceof ReportRuleConfigurationException) {
                throw (ReportRuleConfigurationException) t;
            }
            throw new ReportRuleConfigurationException(t);
        }
    }

    @Override
    public boolean isValid(FightReport c) {
        try {
            for (String t : tribes) {
                if (t.equals(c.getAttacker().getName())) {
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
        result.append("Angreifer ").append(tribes.toString());
        return result.toString();
    }
}
