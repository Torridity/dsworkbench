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
import java.util.Iterator;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class OffVillage extends Village implements Source {

    /**
     * The source's coordinate.
     */
    protected int wares;
    protected int ordered;
    protected ArrayList<Order> orders = new ArrayList<Order>();

    public OffVillage(Coordinate c, int wares) {
        this.c = c;
        this.wares = wares;
        this.ordered = 0;
    }

    public String toString() {
        return this.c.toString();
    }

    public boolean mappingExists(Destination dest) {
        Iterator<Order> i = this.orders.iterator();
        boolean contains = false;
        while (i.hasNext()) {
            Order o = i.next();
            contains = o.getDestination().equals(dest);
        }

        return contains;
    }

    public void addOrder(Destination d, int amount) {
        if (amount == 0) {
            return;
        }
        Iterator<Order> i = this.orders.iterator();
        while (i.hasNext()) {
            Order o = i.next();
            if (o.getDestination() == d) {
                o.setAmount(o.getAmount() + amount);
                this.ordered += amount;
                //		System.out.println("Increased order (amount: " + Integer.toString(amount) + ") at " + this.c.toString() + " to " + ((TargetVillage) d).getC().toString());

                return;
            }
        }

        this.orders.add(new Order(d, amount));
        this.ordered += amount;
        //	System.out.println("Added order (amount: " + Integer.toString(amount) + ") at " + this.c.toString() + " to " + ((TargetVillage) d).getC().toString());
    }

    public void removeOrder(Destination d, int amount) {
        Iterator<Order> i = this.orders.iterator();
        while (i.hasNext()) {
            Order o = i.next();
            if (o.getDestination() == d) {
                o.setAmount(o.getAmount() - amount);
                this.ordered -= amount;
                //	System.out.println("Decreased order (amount: " + Integer.toString(amount) + ") at " + this.c.toString() + " to " + ((TargetVillage) d).getC().toString());
                return;
            }
        }
    }

    public int removeEmptyOrders() {
        return 0;
    }

    public void removeOrder(Order o) {
        //	System.out.println("Removed order at " + this.c.toString() + " to " + ((TargetVillage)o.getDestination()).getC().toString());
        this.ordered -= o.getAmount();
        this.orders.remove(o);
    }

    public int waresAvailable() {
        return this.wares - this.ordered;
    }

    public int waresAvailable(Destination d) {
        Iterator<Order> i = this.orders.iterator();
        while (i.hasNext()) {
            Order o = i.next();
            if (o.getDestination().equals(d)) {
                return 0;
            }
        }
        return 1;
    }

    public int getOrdered() {
        return ordered;
    }

    public ArrayList<Order> getOrders() {
        return this.orders;
    }
}
