/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Village;

/**
 *
 * @author Charon
 */
public class VillageNode extends AbstractTreeNode {

    public VillageNode(Village pVillage) {
        super(pVillage);
    }

    public Village getDSUserObject() {
        return (Village) super.getUserObject();
    }

    @Override
    public boolean isAllyNode() {
        return false;
    }

    @Override
    public boolean isTribeNode() {
        return false;
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
        return true;
    }
}
