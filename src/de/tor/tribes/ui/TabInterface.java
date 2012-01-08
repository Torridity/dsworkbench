/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Tag;
import de.tor.tribes.ui.panels.TroopTableTab.TRANSFER_TYPE;
import java.util.List;

/**
 *
 * @author Torridity
 */
public interface TabInterface {

    public void deregister();

    public void updateFilter(final List<Tag> groups, final boolean pRelation, final boolean pFilterRows);

    public void transferSelection(TRANSFER_TYPE pType);

    public void updateSet();

    public void deleteSelection();

    public void centerVillageInGame();

    public void openPlaceInGame();

    public void centerVillage();

    public void updateSelectionInfo();

    public void refillSupports();
}
