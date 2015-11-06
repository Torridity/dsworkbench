/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import java.util.List;

/**
 *
 * @author Charon
 */
public class AllyFilter implements ConquerFilterInterface {

    private List<Ally> validAllies = null;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            validAllies = (List<Ally>) pFilterComponent;
        } catch (Exception e) {
            validAllies = null;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        if (validAllies == null) {
            return false;
        }
        Tribe t = DataHolder.getSingleton().getTribes().get(pConquer.getWinner());
        if (t == null) {
            return false;
        }
        if (t.getAlly() == null) {
            return validAllies.contains(NoAlly.getSingleton());
        } else {
            return validAllies.contains(t.getAlly());
        }
    }
}
