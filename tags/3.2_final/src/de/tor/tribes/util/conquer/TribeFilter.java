/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Tribe;
import java.util.List;

/**
 *
 * @author Charon
 */
public class TribeFilter implements ConquerFilterInterface {

    private List<Tribe> validTribes = null;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            validTribes = (List<Tribe>) pFilterComponent;
        } catch (Exception e) {
            validTribes = null;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        if (validTribes == null) {
            return true;
        }
        Tribe t = DataHolder.getSingleton().getTribes().get(pConquer.getWinner());
        if (t == null) {
            return false;
        }

        return validTribes.contains(t);
    }
}
