/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.util.algo.types;

/**
 *
 * @author Robert Nitsch <dev@robertnitsch.de>
 */
public class TargetVillage extends Village implements Destination {
	/**
	 * The source's coordinate.
	 */
	protected int needs;
	protected int ordered;

	public TargetVillage(Coordinate c, int needs) {
		this.c = c;
		this.needs = needs;
	}

	public String toString() {
		return this.c.toString();
	}

	public int remainingNeeds() {
		return this.needs-this.ordered;
	}

	public void addOrdered(int amount) {
		this.ordered += amount;
	}

	public int getNeeded() {
		return needs;
	}

	public void setNeeded(int needed) {
		this.needs = needed;
	}
}

