/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Ally;

/**
 *
 * @author Charon
 */
public class AllyNode extends AbstractTreeNode {

    public AllyNode(Ally pAlly) {
        super(pAlly);
    }

    public Ally getDSUserObject() {
        return (Ally) super.getUserObject();
    }

    @Override
    public boolean isAllyNode() {
        return true;
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
        return false;
    }
}
