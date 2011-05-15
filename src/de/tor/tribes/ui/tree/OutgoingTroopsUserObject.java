/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Date;
import java.util.Hashtable;

/**
 *
 * @author Torridity
 */
public class OutgoingTroopsUserObject {

    private VillageTroopsHolder holder = null;

    public OutgoingTroopsUserObject(Village parent, Village pTarget, Hashtable<UnitHolder, Integer> pAmounts) {
        holder = new VillageTroopsHolder(pTarget, new Date(0));
        holder.setTroops(pAmounts);
    }

    public VillageTroopsHolder getTroopsHolder() {
        return holder;
    }
}
