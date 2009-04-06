/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Tribe;

/**
 *
 * @author Charon
 */
public class TribeNode extends AbstractTreeNode {

    public TribeNode(Tribe pTribe) {
        super(pTribe);
    }

    public Tribe getDSUserObject() {
        return (Tribe) super.getUserObject();
    }

    @Override
    public boolean isAllyNode() {
        return false;
    }

    @Override
    public boolean isTribeNode() {
        return true;
    }

    @Override
    public boolean isTagNode() {
        return false;
    }

    @Override
    public boolean isContinentNode() {
        return false;
    }

    @Override
    public boolean isVillageNode() {
        return false;
    }
}
