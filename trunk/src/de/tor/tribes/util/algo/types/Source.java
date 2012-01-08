/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo.types;

import java.util.List;

/**
 * Sources passed to the algorithm have to implement this interface.
 *
 * Each source has an amount of available wares. Every source expected to
 * internally store a List of Orders. Orders consist of the target (a Destination)
 * and the amount of wares, which is actually ordered.
 *
 * Consider to ensure, that there is at most 1 Order per Destination.
 *
 * Example:
 * addOrder(myDestination, 3) will create a new Order and store it in the internal List.
 * addOrder(myDestination, 1) should increase the amount of ordered wares of the
 * Order object created before, instead of creating an additional Order object.
 *
 * Following this recommendation will make it much easier to implement the removeOrder
 * methods (think about it).
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 * @see stp.Order
 * @see stp.Destination
 */
public interface Source {

    /**
     * Should add a new order to destination d or increase an order to
     * destination d, if such an order already exists. After all,
     * there should be only one order per destination!
     * In fact, you dont have to follow this recommendation, but implementation
     * will be far easier, if you do. (See stp.Source documentation also
     * for a more detailed explanation.)
     *
     * @param d
     * @param amount
     */
    public void addOrder(Destination d, int amount);

    /**
     * Decreases the amount of ordered wares to destination d.
     *
     * @param d
     * @param amount
     */
    public void removeOrder(Destination d, int amount);

    /**
     * Removes a whole order.
     *
     * @param o
     */
    public void removeOrder(Order o);

    /**
     * Returns the amount of wares, which are not yet ordered (and therefore
     * still available).
     *
     * @return
     */
    public int waresAvailable();

    public int waresAvailable(Destination d);

    /**
     * Returns the amount of ordered wares.
     *
     * @return
     */
    public int getOrdered();

    /**
     * Returns the internal list of orders.
     *
     * @return
     */
    public List<Order> getOrders();

    public int removeEmptyOrders();

    public boolean mappingExists(Destination d);
}
