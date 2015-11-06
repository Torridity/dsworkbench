/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.util.Comparator;

/**
 *
 * @author Torridity
 */
public class SlashComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        try {
            String s1 = o1.split("/")[0];
            String s2 = o2.split("/")[0];
            return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
        } catch (Exception e) {
            return 0;
        }
    }
}
