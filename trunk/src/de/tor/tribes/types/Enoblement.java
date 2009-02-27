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
public class Enoblement {

    private int iCleaners = 0;
    private Village mTarget = null;
    private List<Village> cleanSources = null;
    private List<Village> snobSources = null;

    public Enoblement(Village pTarget, int pCleanOffs) {
        mTarget = pTarget;
        iCleaners = pCleanOffs;
        cleanSources = new LinkedList<Village>();
        snobSources = new LinkedList<Village>();
    }

    public void addCleanOff(Village pSource) {
        cleanSources.add(pSource);
    }

    public void addSnob(Village pSource) {
        snobSources.add(pSource);
    }

    public boolean snobDone() {
        return (snobSources.size() == 4);
    }

    public boolean offDone() {
        return (cleanSources.size() == iCleaners);
    }
}