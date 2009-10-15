/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Tribe;

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

        if (winnerAlly.getTag().toLowerCase().equals(loserAlly.getTag().toLowerCase()) ||
                winnerAlly.getTag().toLowerCase().indexOf(loserAlly.getTag().toLowerCase()) >= 0 ||
                loserAlly.getTag().toLowerCase().indexOf(winnerAlly.getTag().toLowerCase()) >= 0) {
            internal = true;
        }
        if (!internal) {
            return true;
        }
        return show;
    }
}
