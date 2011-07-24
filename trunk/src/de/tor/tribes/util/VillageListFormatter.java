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
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Torridity
 */
public class VillageListFormatter {

    public static String format(List<Village> pVillages, String pPattern, boolean pUseBBCode) {
        StringBuffer b = new StringBuffer();
        NumberFormat nf = NumberFormat.getInstance();
        NumberFormat nf2 = NumberFormat.getInstance();
        nf2.setMinimumIntegerDigits(3);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        int cnt = 1;
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
            line = StringUtils.replace(line, "%CNT%", nf2.format(cnt));
            if (pUseBBCode) {
                line = StringUtils.replace(line, "%TRIBE%", t.toBBCode());
            } else {
                line = StringUtils.replace(line, "%TRIBE%", t.toString());
            }

            if (pUseBBCode) {
                line = StringUtils.replace(line, "%ALLY%", a.toBBCode());
            } else {
                line = StringUtils.replace(line, "%ALLY%", a.toString());
            }
            if (pUseBBCode) {
                line = StringUtils.replace(line, "%VILLAGE%", v.toBBCode());
            } else {
                line = StringUtils.replace(line, "%VILLAGE%", v.toString());
            }

            line = StringUtils.replace(line, "%X%", nf.format(v.getX()));
            line = StringUtils.replace(line, "%Y%", nf.format(v.getY()));
            line = StringUtils.replace(line, "%POINTS%", nf.format(v.getPoints()));
            b.append(line).append("\n");
            cnt++;
        }
        return b.toString();
    }

   
}
