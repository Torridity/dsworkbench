/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo.types;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class MerchantDestination extends Village implements Destination, Comparable<MerchantDestination> {

    /**
     * The source's coordinate.
     */
    protected int needs;
    protected int ordered;

    public MerchantDestination(Coordinate c, int needs) {
        this.c = c;
        this.needs = needs;
    }

    @Override
    public String toString() {
        return this.c.toString();
    }

    @Override
    public int remainingNeeds() {
        return this.needs - this.ordered;
    }

    @Override
    public void addOrdered(int amount) {
        this.ordered += amount;
    }

    @Override
    public int getNeeded() {
        return needs;
    }

    public void setNeeded(int needed) {
        this.needs = needed;
    }

    @Override
    public int compareTo(MerchantDestination o) {
        return getC().compareTo(o.getC());
    }
}

