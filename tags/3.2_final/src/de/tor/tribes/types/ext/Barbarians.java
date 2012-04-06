/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.ext;

import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import java.text.NumberFormat;

/**
 *
 * @author Charon
 */
public class Barbarians extends Tribe {

    private static Barbarians SINGLETON = null;

    public static synchronized Barbarians getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new Barbarians();
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

    public int getAllyID() {
        return BarbarianAlly.getSingleton().getId();
    }

    public short getVillages() {
        return 0;
    }

    public double getPoints() {
        return 0;
    }

    public int getRank() {
        return 0;
    }

    public Ally getAlly() {
        return BarbarianAlly.getSingleton();
    }

    public String getToolTipText() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<html><table style='border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;'>";
        res += "<tr><td><b>Name:</b> </td><td>" + getName() + "</td></tr>";
        res += "</table></html>";
        return res;
    }
}
