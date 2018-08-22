/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.ui.tree;

import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author Torridity
 */
public class NodeCellRenderer extends DefaultTreeCellRenderer {

    private ImageIcon ally = null;
    private ImageIcon tribe = null;
    private ImageIcon village = null;
    private ImageIcon tag = null;

    public NodeCellRenderer() {
        ally = new ImageIcon("./graphics/icons/def.png");
        tribe = new ImageIcon("./graphics/icons/troops.png");
        village = new ImageIcon("./graphics/icons/village.png");
        tag = new ImageIcon("./graphics/icons/tag16.png");
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

        try {
            MutableTreeNode node = ((MutableTreeNode) value);
            if (node instanceof AllyNode) {
                setIcon(ally);
                Ally a = ((AllyNode) node).getUserObject();
                setText(a.toString() + " [" + node.getChildCount() + " Spieler]");
            } else if (node instanceof TribeNode) {
                setIcon(tribe);
                Tribe t = ((TribeNode) node).getUserObject();
                int cnt = node.getChildCount();
                setText(t.toString() + " [" + cnt + " " + ((cnt == 1) ? "Dorf/Gruppe]" : "Dörfer/Gruppen]"));
            } else if (node instanceof VillageNode) {
                setIcon(village);
                Village v = ((VillageNode) node).getUserObject();
                setText(v.toString());
            } else if (node instanceof TagNode) {
                setIcon(tag);
                Tag v = ((TagNode) node).getUserObject();
                int cnt = node.getChildCount();
                setText(v.toString() + " [" + cnt + " " + ((cnt == 1) ? "Dorf]" : "Dörfer]"));
            } else if (node instanceof SelectionTreeRootNode) {
                int cnt = node.getChildCount();
                boolean flat = false;
                try {
                    AllyNode n = (AllyNode) node.getChildAt(0);
                } catch (Exception e) {
                    flat = true;
                }
                if (flat) {
                    setText(((SelectionTreeRootNode) node).getUserObject() + " [" + cnt + " " + ((cnt == 1) ? "Dorf]" : "Dörfer]"));
                } else {
                    setText(((SelectionTreeRootNode) node).getUserObject() + " [" + cnt + " " + ((cnt == 1) ? "Stamm]" : "Stämme]"));
                }
            } else {
            }
        } catch (Exception ignored) {
        }
        return this;
    }
}
