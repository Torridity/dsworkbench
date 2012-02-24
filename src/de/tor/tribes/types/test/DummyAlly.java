/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.test;

import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
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
    public Tribe[] getTribes() {
        return t.toArray(new Tribe[1]);
    }
}
