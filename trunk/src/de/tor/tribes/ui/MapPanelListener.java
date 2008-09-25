/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui;

import de.tor.tribes.types.Village;

/**
 *
 * @author Jejkal
 */
public interface MapPanelListener {

    public void fireVillageAtMousePosChangedEvent(Village pVillage);
    public void fireDistanceEvent(Village pSource, Village pTarget);
    public void fireScrollEvent(int pX, int pY);
}
