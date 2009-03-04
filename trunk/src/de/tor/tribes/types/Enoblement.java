/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.DSCalculator;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class Enoblement extends AbstractTroopMovement {

    private List<Village> snobSources = null;
    public static final Comparator<Enoblement> DISTANCE_SORTER = new SnobDistanceSort();

    public Enoblement(Village pTarget, int pCleanOffs, int pMaxOffs) {
        super(pTarget, pCleanOffs, pMaxOffs);
        snobSources = new LinkedList<Village>();
    }

    public int getNumberOfCleanOffs() {
        return getMinOffs();
    }

    public void addCleanOff(UnitHolder pUnit, Village pSource) {
        addOff(pUnit, pSource);
    }

    public void addSnob(Village pSource) {
        snobSources.add(pSource);
    }

    public boolean snobDone() {
        return (snobSources.size() == 4);
    }

    public boolean offDone() {
        return offValid();
    }

    public List<Village> getSnobSources() {
        return snobSources;
    }

    private static class SnobDistanceSort implements Comparator<Enoblement>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Enoblement e1, Enoblement e2) {

            double d1 = DSCalculator.calculateDistance(e1.getSnobSources().get(3), e1.getTarget());
            double d2 = DSCalculator.calculateDistance(e2.getSnobSources().get(3), e2.getTarget());
            return Double.compare(d1, d2);
        }
    }
}