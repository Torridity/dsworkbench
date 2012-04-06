/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.php;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.util.GlobalOptions;
import java.util.Hashtable;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class UnitTableInterface {

    private static final String[] units = new String[]{"spear", "sword", "axe", "archer", "spy", "light", "marcher", "heavy", "ram", "catapult", "knight", "snob", "militia"};

    public static String createAttackerUnitTableLink(Hashtable<UnitHolder, Integer> pIn, Hashtable<UnitHolder, Integer> pOut) {
        return createUnitTableLink(pIn, pOut, false);
    }

    public static String createAttackerUnitTableLink(Hashtable<UnitHolder, Integer> pIn) {
        return createUnitTableLink(pIn, false);
    }

    public static String createDefenderUnitTableLink(Hashtable<UnitHolder, Integer> pIn, Hashtable<UnitHolder, Integer> pOut) {
        return createUnitTableLink(pIn, pOut, true);
    }

    public static String createDefenderUnitTableLink(Hashtable<UnitHolder, Integer> pIn) {
        return createUnitTableLink(pIn, false);
    }

    public static String createUnitTableLink(Hashtable<UnitHolder, Integer> pIn, Hashtable<UnitHolder, Integer> pOut, boolean pMilitia) {
        StringBuilder b = new StringBuilder();
        b.append("http://www.dsworkbench.de/tools/unitTable.php?in=");

        for (int i = 0; i < units.length; i++) {
            UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(units[i]);
            if (!u.equals(UnknownUnit.getSingleton()) && (pMilitia || !u.getPlainName().equals("militia"))) {
                Integer amount = pIn.get(u);
                b.append(i).append(".").append((amount == null) ? 0 : amount);
                if (i < units.length - 1) {
                    b.append("_");
                }
            }
        }

        b.append("&out=");
        for (int i = 0; i < units.length; i++) {
            UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(units[i]);
            if (!u.equals(UnknownUnit.getSingleton()) && (pMilitia || !u.getPlainName().equals("militia"))) {
                Integer amount = pOut.get(u);
                b.append(i).append(".").append((amount == null) ? 0 : amount);
                b.append("_");
            }
        }
        String result = b.toString();
        return result.substring(0, result.length() - 1);
    }

    public static String createUnitTableLink(Hashtable<UnitHolder, Integer> pIn, boolean pMilitia) {
        StringBuilder b = new StringBuilder();
        b.append("http://www.dsworkbench.de/tools/unitTable.php?in=");

        for (int i = 0; i < units.length; i++) {
            UnitHolder u = DataHolder.getSingleton().getUnitByPlainName(units[i]);
            if (!u.equals(UnknownUnit.getSingleton()) && (pMilitia || !u.getPlainName().equals("militia"))) {
                Integer amount = pIn.get(u);
                b.append(i).append(".").append((amount == null) ? 0 : amount);
                b.append("_");
            }
        }

        String result = b.toString();
        return result.substring(0, result.length() - 1);
    }

    public static void main(String[] args) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(new org.apache.log4j.PatternLayout("%d - %-5p - %-20c (%C [%L]) - %m%n")));
        Logger.getRootLogger().setLevel(Level.ERROR);
        GlobalOptions.setSelectedServer("de77");
        DataHolder.getSingleton().loadData(false);
        Hashtable<UnitHolder, Integer> in = new Hashtable<UnitHolder, Integer>();
        for (UnitHolder h : DataHolder.getSingleton().getUnits()) {
            in.put(h, 100);
        }


        Hashtable<UnitHolder, Integer> out = new Hashtable<UnitHolder, Integer>();
        out.put(DataHolder.getSingleton().getUnitByPlainName("axe"), 100);
        System.out.println(createDefenderUnitTableLink(in));
    }
}
