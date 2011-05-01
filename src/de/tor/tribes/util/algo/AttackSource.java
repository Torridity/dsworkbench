/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo;

import java.util.ArrayList;
import java.util.Iterator;

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
    protected ArrayList<Order> orders = new ArrayList<Order>();

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

        Iterator<Order> i = this.orders.iterator();
        while (i.hasNext()) {
            Order o = i.next();
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
        Iterator<Order> i = this.orders.iterator();
        while (i.hasNext()) {
            Order o = i.next();
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
