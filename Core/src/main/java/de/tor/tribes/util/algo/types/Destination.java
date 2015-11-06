/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo.types;

/**
 * Destinations passed to the algorithm have to implement this interface.
 *
 * Each destination has an amount of needed wares and another amount of
 * ordered wares (which should be 0 at first). It doesnt have to care
 * about the Orders, since Orders are managed by the Sources (see stp.Source).
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 * @see stp.Source
 */
public interface Destination {

    public String toString();

    /**
     * Returns the amount of wares which are still needed.
     *
     * Example: The destination needs 5 wares and has already ordered 3.
     *  		Then the destination still needs 2 wares. ;)
     *
     * @return
     */
    public int remainingNeeds();

    /**
     * Increases the amount of ordered wares.
     *
     * @param amount the amount to add
     */
    public void addOrdered(int amount);

    /**
     * Returns the original (!) need of wares. Independent from the amount of
     * wares, which already have been ordered.
     *
     * @return
     */
    public int getNeeded();
}

