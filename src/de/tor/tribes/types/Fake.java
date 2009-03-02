/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class Fake {

    private int maxAttacks = 0;
    private Village mTarget = null;
    private List<Village> sources = null;

    public Fake(Village pTarget, int pMaxAttacks) {
        mTarget = pTarget;
        maxAttacks = pMaxAttacks;
        sources = new LinkedList<Village>();
    }

    public void addOff(Village pSource) {
        sources.add(pSource);
    }

    public boolean offDone() {
        return (sources.size() == maxAttacks);
    }

    public Village getTarget() {
        return mTarget;
    }

    public List<Village> getOffSources() {
        return sources;
    }
}
