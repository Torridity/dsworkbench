/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Charon
 */
public abstract class AbstractTreeNode extends DefaultMutableTreeNode {

    public AbstractTreeNode(Object o) {
        super(o);
    }

    public abstract boolean isAllyNode();

    public abstract boolean isTribeNode();

    public abstract boolean isTagNode();

    public abstract boolean isContinentNode();

    public abstract boolean isVillageNode();
}
