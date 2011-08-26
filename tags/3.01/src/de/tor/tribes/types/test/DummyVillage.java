/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Village;

/**
 *
 * @author Torridity
 */
public class DummyVillage extends Village {

    public DummyVillage() {
        this((short) 0, (short) 0);
    }

    public DummyVillage(short pX, short pY) {
        super();
        setX(pX);
        setY(pY);
        setTribe(new DummyTribe());
    }

    @Override
    public int getPoints() {
        return 10019;
    }

    @Override
    public String getName() {
        return "DummyVillage";
    }
}
