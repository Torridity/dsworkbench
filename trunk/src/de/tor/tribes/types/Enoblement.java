/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.DSCalculator;
import java.util.Comparator;
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
    public static final Comparator<Enoblement> DISTANCE_SORTER = new SnobDistanceSort();

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

    public Village getTarget() {
        return mTarget;
    }

    public List<Village> getSnobSources() {
        return snobSources;
    }

    public List<Village> getCleanSources() {
        return cleanSources;
    }

    private static class SnobDistanceSort implements Comparator<Enoblement>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Enoblement e1, Enoblement e2) {
            double d1 = DSCalculator.calculateDistance(e1.getSnobSources().get(0), e1.getSnobSources().get(3));
            double d2 = DSCalculator.calculateDistance(e2.getSnobSources().get(0), e2.getSnobSources().get(3));
            return Double.compare(d1, d2);
        }
    }
}