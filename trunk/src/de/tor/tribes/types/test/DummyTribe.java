/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
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
        for (int i = 0; i < 100; i++) {
            DummyVillage vi = new DummyVillage((short) (i - 5), (short) (i + 5));
            v.add(vi);
        }
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
