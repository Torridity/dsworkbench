/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.io;

/**
 *
 * @author Jejkal
 */
public abstract class AbstractDataLoader {

    public abstract boolean loadData(boolean pDownload);
    public abstract boolean downloadData();
    
}
