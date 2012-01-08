/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.windows.TroopSplitDialog.TroopSplit;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Torridity
 */
public class TroopSplitListCellRenderer extends JLabel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        try {
            setOpaque(true);

            TroopSplit value = (TroopSplit) pValue;

            StringBuilder builder = new StringBuilder();
            builder.append(value.getVillage());
            builder.append(" [");
            int splitCount = value.getSplitCount();
            builder.append(splitCount);
            builder.append((splitCount == 1) ? " Split" : " Splits");
            builder.append("]");
            setText(builder.toString());
            if (pSelected) {
                setForeground(list.getSelectionForeground());
                super.setBackground(list.getSelectionBackground());
            } else {
                if (splitCount == 0) {
                    setForeground(Color.RED);
                } else if (splitCount == 1) {
                    setForeground(Color.ORANGE.darker());
                } else {
                    setForeground(Color.GREEN.darker());
                }
                if (pIndex % 2 == 0) {
                    setBackground(Constants.DS_ROW_B);
                } else {
                    setBackground(Constants.DS_ROW_A);
                }
            }
        } catch (Exception e) {
            //cast problem
            setText("-Fehler-");
        }
        return this;

    }
}
