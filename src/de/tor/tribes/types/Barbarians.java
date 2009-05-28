/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

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
}
