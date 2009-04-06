/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Tag;

/**
 *
 * @author Charon
 */
public class TagNode extends AbstractTreeNode {

    public final static String NO_TAG = "Kein Tag";

    public TagNode(Tag pTag) {
        super(pTag);
    }

    public Tag getDSUserObject() {
        return (Tag) super.getUserObject();
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
        return true;
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
