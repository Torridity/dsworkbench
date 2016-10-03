/**
 * 
 */
package de.tor.tribes.util;

import java.util.Observable;

/**
 * @author Patrick
 *
 */
public class GenericObservable extends Observable {

	/**
	 * 
	 */
	public GenericObservable() {
		super();
	}

	public void setChanged(){
		super.setChanged();
	}
}
