/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

/**
 *
 * @author Jejkal
 */
public class DSWorkbenchTreeExpansionListener implements TreeExpansionListener {

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        try {
            DSWorkbenchTreeNode lastNode = (DSWorkbenchTreeNode) event.getPath().getLastPathComponent();
            if (lastNode.isTribeNode()) {
                Tribe t = (Tribe) lastNode.getUserObject();
                for (Village v : t.getVillageList()) {
                    lastNode.add(new DSWorkbenchTreeNode<Village>(v));
                }
            } else if (lastNode.isAllyNode()) {
                Ally a = (Ally) lastNode.getUserObject();
                for (Tribe t : a.getTribes()) {
                    lastNode.add(new DSWorkbenchTreeNode<Tribe>(t));
                }
            } else {
                System.out.println(lastNode.getUserObject().getClass());
            }
        } catch (Exception e) {
            System.out.println("Misc");
        }
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        try {
            DSWorkbenchTreeNode lastNode = (DSWorkbenchTreeNode) event.getPath().getLastPathComponent();
            if (lastNode.isTribeNode()) {
                lastNode.removeAllChildren();
            } else if (lastNode.isTribeNode()) {
                lastNode.removeAllChildren();
            }
        } catch (Exception e) {
            System.out.println("Misc");
        }
    }
}
