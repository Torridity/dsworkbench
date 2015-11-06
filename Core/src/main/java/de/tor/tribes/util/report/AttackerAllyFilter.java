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
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Torridity
 */
public class AttackerAllyFilter implements ReportRuleInterface {

    private List<String> allies = null;

    @Override
    public void setup(Object pFilterComponent) throws ReportRuleConfigurationException {


        try {
            String[] allySplit = ((String) pFilterComponent).split(";");
            allies = new LinkedList<String>();
            if (allySplit == null || allySplit.length == 0) {
                throw new ReportRuleConfigurationException("Kein Stammestag gefunden");
            }
            for (String split : allySplit) {
                if (split != null) {
                    Ally a = DataHolder.getSingleton().getAllyByTagName(split.trim());
                    if (a != null) {
                        allies.add(split.trim());
                    }
                }
            }
            if (allies.isEmpty()) {
                throw new ReportRuleConfigurationException("Kein Stammestag gefunden");
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
