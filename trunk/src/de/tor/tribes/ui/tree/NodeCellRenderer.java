/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Jejkal
 */
public class NodeCellRenderer extends DefaultTreeCellRenderer {

    private ImageIcon ally = null;
    private ImageIcon tribe = null;

    public NodeCellRenderer() {
        ally = new ImageIcon("./graphics/icons/def.png");
        tribe = new ImageIcon("./graphics/icons/troops.png");
    }

    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        try {
            DSWorkbenchTreeNode node = ((DSWorkbenchTreeNode) value);
            if (node.isAllyNode()) {
                setIcon(ally);
                setToolTipText("AllyName");
            } else if (node.isTribeNode()) {
                setIcon(tribe);
                setToolTipText("TribeName");
            } else {
                setToolTipText("");
            }
        } catch (Exception e) {
            
        }
        return this;
    }
}
