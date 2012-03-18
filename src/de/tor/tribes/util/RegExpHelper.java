/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;

/**
 *
 * @author Torridity
 */
public class RegExpHelper {

    public static String getTroopsPattern(boolean pTrailingSpace, boolean pMilitia) {
        // ([0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+)
        StringBuilder b = new StringBuilder();
        if (pTrailingSpace) {
            b.append("(\\s+[0-9]+");
        } else {
            b.append("([0-9]+");
        }
        for (int i = 1; i < DataHolder.getSingleton().getUnits().size(); i++) {
            if (pMilitia || !DataHolder.getSingleton().getUnits().get(i).getPlainName().equals("militia")) {
                b.append("\\s+[0-9]+");
            }
        }
        b.append(")");
        return b.toString();
    }
}
