/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;

/**
 *
 * @author Jejkal
 */
public class DistanceMapping implements Comparable<DistanceMapping> {

    private Village source = null;
    private Village target = null;
    private double distance = 0.0;

    public DistanceMapping(Village pSource, Village pTarget) {
        source = pSource;
        target = pTarget;
        distance = DSCalculator.calculateDistance(pSource, pTarget);
    }

    public Village getSource() {
        return source;
    }

    public Village getTarget() {
        return target;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(DistanceMapping o) {
        if (getDistance() < o.getDistance()) {
            return -1;
        } else if (getDistance() > o.getDistance()) {
            return 1;
        }
        return 0;
    }

    public String toString(){
        String result = source + " -> " + target + " (" + distance + ")";
        return result;
    }
}
