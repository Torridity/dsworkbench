/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class UnitListCellRenderer extends DefaultListCellRenderer {

    public UnitListCellRenderer() {
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        Component c = super.getListCellRendererComponent(list, pValue, pIndex, pSelected, pHasFocus);
        try {
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            if (pValue == null) {
                //no icon!?
                ((JLabel) c).setText("-");
                ((JLabel) c).setIcon(null);
            } else {
                ((JLabel) c).setText("");
                ((JLabel) c).setIcon(ImageManager.getUnitIcon((UnitHolder) pValue));
            }
        } catch (Exception e) {
            //cast problem
        }

        return c;

    }
}
