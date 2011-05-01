/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class DummyAlly extends Ally {

    List<Tribe> t = new LinkedList<Tribe>();

    public DummyAlly() {
        t.add(new DummyTribe());
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public short getMembers() {
        return 1;
    }

    @Override
    public List<Tribe> getTribes() {
        return t;
    }
}
