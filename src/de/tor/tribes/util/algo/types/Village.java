/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.util.algo.types;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public abstract class Village {
	protected Coordinate c;

	public double distanceTo(Village l) {
		return l.getC().distanceTo(this.c);
	}

	public Coordinate getC() {
		return c;
	}

	public void setC(Coordinate c) {
		this.c = c;
	}
}