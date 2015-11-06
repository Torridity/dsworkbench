/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DummyTribe extends Tribe {

    private List<Village> v = new LinkedList<Village>();

    public DummyTribe() {
        super();
    }

    
    @Override
    public String getName() {
        return "Dummy";
    }

    @Override
    public Ally getAlly() {
        return super.getAlly();
    }

    @Override
    public Village[] getVillageList() {
        return v.toArray(new Village[v.size()]);
    }

    @Override
    public short getVillages() {
        return (short) v.size();
    }
}
