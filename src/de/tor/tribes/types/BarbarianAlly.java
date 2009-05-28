/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

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

    public String getName() {
        return "Barbaren";
    }

    public String getTag() {
        return "Barb";
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
}
