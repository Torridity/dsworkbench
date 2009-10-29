/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

/**
 *
 * @author Jejkal
 */
public class NoAlly extends Ally {

    private static NoAlly SINGLETON = null;

    public static synchronized NoAlly getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoAlly();
        }
        return SINGLETON;
    }

    public int getId() {
        return -1;
    }

    public String getName() {
        return "Kein Stamm";
    }

    public String getTag() {
        return "-";
    }

    public short getMembers() {
        return 0;
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
}
