/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

/**
 *
 * @author Charon
 */
public class ContinentNode extends AbstractTreeNode {

    public ContinentNode(String pContinent) {
        super(pContinent);
    }

    @Override
    public String getUserObject() {
        return (String) super.getUserObject();
    }

    public int getContinent() {
        String cont = getUserObject();
        cont = cont.replaceAll("K", "");
        return Integer.parseInt(cont);
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
        return true;
    }

    @Override
    public boolean isVillageNode() {
        return false;
    }
}
