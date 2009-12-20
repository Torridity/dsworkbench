/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class AttackTypeListCellRenderer extends DefaultListCellRenderer {

    private List<ImageIcon> icons = null;

    public AttackTypeListCellRenderer() {
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
        } catch (Exception e) {
            icons = null;
        }
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        Component c = super.getListCellRendererComponent(list, pValue, pIndex, pSelected, pHasFocus);
        try {
            Integer type = (Integer) pValue;
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            if (type == 0) {
                //no icon!?
                ((JLabel) c).setText("-");
                ((JLabel) c).setIcon(null);
            } else {
                int pos = type - 1;
                if (pos >= 0) {
                    ((JLabel) c).setText("");
                    ((JLabel) c).setIcon(icons.get(pos));
                } else {
                    ((JLabel) c).setText("-");
                    ((JLabel) c).setIcon(null);
                }
            }
        } catch (Exception e) {
            //cast problem
        }

        return c;

    }
}
