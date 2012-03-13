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
 * @author Jejkal
 */
public class NoAlly extends Ally {
    
    private static NoAlly SINGLETON = null;
    private List<Tribe> tribes = null;
    
    public static synchronized NoAlly getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoAlly();
        }
        return SINGLETON;
    }
    
    public NoAlly() {
        tribes = new LinkedList<Tribe>();
    }
    
    public void reset() {
        tribes.clear();
    }
    
    @Override
    public void addTribe(Tribe t) {
        tribes.add(t);
    }
    
    @Override
    public int getId() {
        return -1;
    }
    
    @Override
    public String getName() {
        return "Kein Stamm";
    }
    
    @Override
    public String getTag() {
        return "-";
    }
    
    @Override
    public Tribe[] getTribes() {
        return tribes.toArray(new Tribe[tribes.size()]);
    }
    
    @Override
    public short getMembers() {
        return (short) tribes.size();
    }
    
    @Override
    public double getPoints() {
        return 0;
    }
    
    @Override
    public int getRank() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "Kein Stamm";
    }
    
    @Override
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
