/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @see Order
 * @see Destination
 */
public interface Source {

    /**
     * Should add a new order to destination d or increase an order to
     * destination d, if such an order already exists. After all,
     * there should be only one order per destination!
     * In fact, you dont have to follow this recommendation, but implementation
     * will be far easier, if you do. (See stp.Source documentation also
     * for a more detailed explanation.)
     */
    void addOrder(Destination d, int amount);

    /**
     * Decreases the amount of ordered wares to destination d.
     */
    void removeOrder(Destination d, int amount);

    /**
     * Removes a whole order.
     */
    void removeOrder(Order o);

    /**
     * Returns the amount of wares, which are not yet ordered (and therefore
     * still available).
     *
     * @return
     */
    int waresAvailable();

    int waresAvailable(Destination d);

    /**
     * Returns the amount of ordered wares.
     *
     * @return
     */
    int getOrdered();

    /**
     * Returns the internal list of orders.
     *
     * @return
     */
    List<Order> getOrders();

    int removeEmptyOrders();

    boolean mappingExists(Destination d);
}
