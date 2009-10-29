/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.io;

/**
 *
 * @author Jejkal
 */
public class DefaultDataLoader extends AbstractDataLoader{

    @Override
    public boolean loadData(boolean pDownload) {
        //check if update needed
        return true;
    }

    @Override
    public boolean downloadData() {
        //check which kind of update (inc/full)
        //--> if data not available do full
        //--> if data 
        return true;
    }

}
