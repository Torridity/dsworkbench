/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.algo.types;

import java.lang.Math;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class Coordinate implements Comparable<Coordinate> {

    protected int x;
    protected int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return Integer.toString(this.x) + "|" + Integer.toString(this.y);
    }

    public double distanceTo(Coordinate coordinate) {
        return Math.sqrt(Math.pow(Math.abs(this.x - coordinate.x), 2) + Math.pow(Math.abs(this.y - coordinate.y), 2));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int compareTo(Coordinate o) {
        if (getX() == o.getX() && getY() == o.getY()) {
            return 0;
        }
        return 1;
    }
}

