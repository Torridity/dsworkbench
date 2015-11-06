/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.io.UnitHolder;

/**
 *
 * @author Torridity
 */
public class DummyUnit extends UnitHolder {

    public DummyUnit() {
        super();    
    }

    @Override
    public String getPlainName() {
        return "ram";
    }

    @Override
    public String getName() {
        return "Ramme";
    }

    @Override
    public double getSpeed() {
        return 30d;
    }

}
