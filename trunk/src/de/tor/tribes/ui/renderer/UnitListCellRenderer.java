/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class UnitListCellRenderer extends JLabel implements ListCellRenderer {

    public UnitListCellRenderer() {
        super();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        //Component c = super.getListCellRendererComponent(list, pValue, pIndex, pSelected, pHasFocus);

        setOpaque(true);
        if (pSelected) {
            setForeground(list.getSelectionForeground());
            super.setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        try {
            setHorizontalAlignment(SwingConstants.CENTER);
            if (pValue == null) {
                //no icon!?
                setText("-");
                setIcon(null);
            } else {
                setText("");
                setIcon(ImageManager.getUnitIcon((UnitHolder) pValue));
            }
        } catch (Exception e) {
            //cast problem
        }

        return this;

    }
}
