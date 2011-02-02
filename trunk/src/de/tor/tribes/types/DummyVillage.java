/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

/**
 *
 * @author Torridity
 */
public class DummyVillage extends Village {

    @Override
    public short getX() {
        return 0;
    }

    @Override
    public short getY() {
        return 0;
    }

    @Override
    public String getName() {
        return "DummyVillage";
    }
}
