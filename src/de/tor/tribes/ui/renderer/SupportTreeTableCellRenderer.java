/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.tree.IncomingTroopsUserObject;
import de.tor.tribes.ui.tree.OutgoingTroopsUserObject;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Torridity
 */
public class SupportTreeTableCellRenderer extends DefaultTreeCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 5593629042737938947L;
    private ImageIcon inIcon = null;
    private ImageIcon outIcon = null;

    public SupportTreeTableCellRenderer() {
        outIcon = new ImageIcon("graphics/icons/move_out.png");
        inIcon = new ImageIcon("graphics/icons/move_in.png");
    }

    @Override
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
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        if (node != null && node.getUserObject() != null && (node.getUserObject() instanceof IncomingTroopsUserObject)) {
            IncomingTroopsUserObject item = (IncomingTroopsUserObject) (node.getUserObject());
            setText(item.getTroopsHolder().getVillage().toString());
            setIcon(inIcon);
        } else if (node != null && node.getUserObject() != null && (node.getUserObject() instanceof OutgoingTroopsUserObject)) {
            OutgoingTroopsUserObject item = (OutgoingTroopsUserObject) (node.getUserObject());
            setText(item.getTroopsHolder().getVillage().toString());
            setIcon(outIcon);
        } else {
            setIcon(null);
        }
        return this;
    }
}
