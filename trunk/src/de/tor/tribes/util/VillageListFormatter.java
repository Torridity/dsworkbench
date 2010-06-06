/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import java.text.NumberFormat;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class VillageListFormatter {

    public static String format(List<Village> pVillages, String pPattern, boolean pUseBBCode) {
        StringBuffer b = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        for (Village v : pVillages) {

            Tribe t = v.getTribe();
            Ally a = v.getTribe().getAlly();
            if (t == null) {
                t = Barbarians.getSingleton();
                a = BarbarianAlly.getSingleton();
            }

            if (a == null) {
                a = NoAlly.getSingleton();
            }

            String line = pPattern;
            if (pUseBBCode) {
                line = line.replaceAll("%TRIBE%", t.toBBCode());
            } else {
                line = line.replaceAll("%TRIBE%", t.toString());
            }

            if (pUseBBCode) {
                line = line.replaceAll("%ALLY%", a.toBBCode());
            } else {
                line = line.replaceAll("%ALLY%", a.toString());
            }
            if (pUseBBCode) {
                line = line.replaceAll("%VILLAGE%", v.toBBCode());
            } else {
                line = line.replaceAll("%VILLAGE%", v.toString());
            }

            line = line.replaceAll("%X%", nf.format(v.getX()));
            line = line.replaceAll("%Y%", nf.format(v.getY()));
            line = line.replaceAll("%POINTS%", nf.format(v.getPoints()));
            b.append(line + "\n");
        }
        return b.toString();
    }

    public static void main(String[] args) {
        String test = "%TRIBE%";
        System.out.println(test.replaceAll("%TRIBE%", "OK"));
    }
}
