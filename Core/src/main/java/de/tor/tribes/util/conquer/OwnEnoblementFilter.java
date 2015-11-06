/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.conquer;

import de.tor.tribes.types.Conquer;

/**
 *
 * @author Charon
 */
public class OwnEnoblementFilter implements ConquerFilterInterface {

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
        boolean own = (pConquer.getWinner() == pConquer.getLoser());
        if (!own) {
            return true;
        }
        return show;
    }
}
