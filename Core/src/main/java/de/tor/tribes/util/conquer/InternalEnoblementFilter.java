/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Tribe;

/**
 *
 * @author Charon
 */
public class InternalEnoblementFilter implements ConquerFilterInterface {

    boolean show = true;

    @Override
    public void setup(Object pFilterComponent) {
        try {
            show = (Boolean) pFilterComponent;
        } catch (Exception e) {
            show = true;
        }
    }

    @Override
    public boolean isValid(Conquer pConquer) {
        Tribe winner = DataHolder.getSingleton().getTribes().get(pConquer.getWinner());
        Tribe loser = DataHolder.getSingleton().getTribes().get(pConquer.getLoser());
        if ((winner == null) || (loser == null)) {
            return true;
        }

        Ally winnerAlly = winner.getAlly();
        Ally loserAlly = loser.getAlly();

        if ((winnerAlly == null || loserAlly == null)) {
            return true;
        }

        boolean internal = false;

        if ((winnerAlly.getId() == loserAlly.getId()) ||
                (loserAlly.getName().toLowerCase().indexOf(winnerAlly.getName().toLowerCase()) > -1 ||
                winnerAlly.getName().toLowerCase().indexOf(loserAlly.getName().toLowerCase()) > -1)) {
            internal = true;
        }
        if (!internal) {
            return true;
        }
        return show;
    }
}
