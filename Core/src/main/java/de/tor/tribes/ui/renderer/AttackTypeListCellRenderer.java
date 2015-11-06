/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class AttackTypeListCellRenderer extends JLabel implements ListCellRenderer {

    private List<ImageIcon> icons = null;

    public AttackTypeListCellRenderer() {
        super();
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/axe.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
            icons.add(new ImageIcon("./graphics/icons/def.png"));
            icons.add(new ImageIcon("./graphics/icons/fake.png"));
            icons.add(new ImageIcon("./graphics/icons/def_fake.png"));
        } catch (Exception e) {
            icons = null;
        }
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        try {
            Integer type = (Integer) pValue;
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            if (pSelected) {
                setForeground(list.getSelectionForeground());
                super.setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            if (type == 0) {
                //no icon!?
                setText("-");
                setIcon(null);
            } else {
                int pos = type - 1;
                if (pos >= 0) {
                    setText("");
                    setIcon(icons.get(pos));
                } else {
                    setText("-");
                    setIcon(null);
                }
            }
        } catch (Exception e) {
            //cast problem
        }

        return this;

    }
}
