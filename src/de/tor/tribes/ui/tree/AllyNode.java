/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Ally;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Charon
 */
public class AllyNode extends DefaultMutableTreeNode {


    public AllyNode() {
        super();
    }

    public AllyNode(Ally pUserObject) {
        super(pUserObject);
    }

    public AllyNode(Ally pUserObject, boolean pAllowsChildren) {
        super(pUserObject, pAllowsChildren);
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        super.insert(child, index);
    //recalc elem count
    }

    @Override
    public void remove(int index) {
        super.remove(index);
    //recalc elem count
    }

    @Override
    public void remove(MutableTreeNode node) {
        super.remove(node);
    //recalc elem count
    }

    public void setUserObject(Ally object) {
        super.setUserObject(userObject);
    }

    @Override
    public Ally getUserObject() {
        return (Ally) super.getUserObject();
    }

    @Override
    public void removeFromParent() {
        super.removeFromParent();
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        super.setParent(newParent);
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return super.getChildAt(childIndex);
    }

    @Override
    public int getChildCount() {
        return super.getChildCount();
    }

    @Override
    public TreeNode getParent() {
        return super.getParent();
    }

    @Override
    public int getIndex(TreeNode node) {
        return super.getIndex(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return super.getAllowsChildren();
    }

    @Override
    public boolean isLeaf() {
        return super.isLeaf();
    }

    @Override
    public Enumeration children() {
        return super.children();
    }
}