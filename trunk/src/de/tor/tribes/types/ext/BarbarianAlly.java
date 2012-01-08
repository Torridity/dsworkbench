/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.ext;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class BarbarianAlly extends Ally {

    private static BarbarianAlly SINGLETON = null;

    public static synchronized BarbarianAlly getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new BarbarianAlly();
        }
        return SINGLETON;
    }

    public int getId() {
        return 0;
    }

    @Override
    public String toBBCode() {
        return "Barbaren";
    }

    public String getName() {
        return "Barbaren";
    }

    public String getTag() {
        return "-";
    }

    public short getMembers() {
        return 0;
    }

    public double getPoints() {
        return 0;
    }

    public int getRank() {
        return 0;
    }

    public List<Tribe> getTribes() {
        return new LinkedList<Tribe>();
    }

    public String toString() {
        return "Barbaren";
    }

    public String getToolTipText() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>";
        res += "<tr><td><b>Stamm:</b> </td><td>" + toString() + "</td></tr>";
        res += "</table></html>";
        return res;
    }
}
