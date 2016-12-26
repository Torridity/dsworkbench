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

import java.util.ArrayList;

/**
 *
 * @author Torridity
 */
public class AttackSource extends Village implements Source {

    /**
     * The source's coordinate.
     */
    protected int wares;
    protected int ordered;
    protected ArrayList<Order> orders = new ArrayList<>();

    public AttackSource(Coordinate c, int wares) {
        this.c = c;
        this.wares = wares;
        this.ordered = 0;
    }

    public String toString() {
        return this.c.toString();
    }

    public void addOrder(Destination d, int amount) {
        if (amount == 0) {
            return;
        }

        for (Order o : this.orders) {
            if (o.getDestination() == d) {
                o.setAmount(o.getAmount() + amount);
                this.ordered += amount;
                System.out.println("Increased order (amount: " + Integer.toString(amount) + ") at " + this.c.toString() + " to " + ((AttackDestination) d).getC().toString());

                return;
            }
        }

        this.orders.add(new Order(d, amount));
        this.ordered += amount;
        System.out.println("Added order (amount: " + Integer.toString(amount) + ") at " + this.c.toString() + " to " + ((AttackDestination) d).getC().toString());
    }

    public void removeOrder(Destination d, int amount) {
        for (Order o : this.orders) {
            if (o.getDestination() == d) {
                o.setAmount(o.getAmount() - amount);
                this.ordered -= amount;
                System.out.println("Decreased order (amount: " + Integer.toString(amount) + ") at " + this.c.toString() + " to " + ((AttackDestination) d).getC().toString());
                return;
            }
        }
    }

    public void removeOrder(Order o) {
        System.out.println("Removed order at " + this.c.toString() + " to " + ((AttackDestination) o.getDestination()).getC().toString());
        this.ordered -= o.getAmount();
        this.orders.remove(o);
    }

    public int waresAvailable() {
        return this.wares - this.ordered;
    }

    public int getOrdered() {
        return ordered;
    }

    public ArrayList<Order> getOrders() {
        return this.orders;
    }

    @Override
    public int waresAvailable(Destination d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int removeEmptyOrders() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean mappingExists(Destination d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
