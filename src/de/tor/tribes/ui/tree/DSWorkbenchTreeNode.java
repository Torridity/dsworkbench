/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Jejkal
 */
public class DSWorkbenchTreeNode<C> extends DefaultMutableTreeNode {

    public DSWorkbenchTreeNode(C o) {
        super(o);
    }

    @Override
    public C getUserObject() {
        return (C)super.getUserObject();
    }

    public boolean isTribeNode() {
        return (getUserObject() instanceof Tribe);
    }

    public boolean isAllyNode() {
        return (getUserObject() instanceof Ally);
    }
}
