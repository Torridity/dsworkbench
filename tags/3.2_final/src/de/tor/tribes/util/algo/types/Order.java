/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo.types;

/**
 * The representation of an Order, consisting of the target Destination
 * and the amount of wares, which is actually meant to be ordered.
 * 
 * Methods are self-explanatory.
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class Order {

    protected Destination destination;
    protected int amount;

    public Order(Destination d, int amount) {
        this.destination = d;
        this.amount = amount;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String toString() {
        String res = "(" + getAmount() + ") -> " + destination;
        return res;
    }
}

