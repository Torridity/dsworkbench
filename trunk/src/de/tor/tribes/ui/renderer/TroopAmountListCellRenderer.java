/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 *
 * @author Jejkal
 */
public class TroopAmountListCellRenderer extends JLabel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        try {
            setOpaque(true);

            String value = (String) pValue;

            String[] amountAndName = value.split(" ");
            String name = amountAndName[1].trim();
            UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName(name);
            ImageIcon icon = ImageManager.getUnitIcon(unit);
            setIcon(icon);
            setHorizontalTextPosition(SwingConstants.LEFT);
            setHorizontalAlignment(SwingConstants.CENTER);
            setText(amountAndName[0]);
            setOpaque(true);
            if (pSelected) {
                setForeground(list.getSelectionForeground());
                super.setBackground(list.getSelectionBackground());
            } else {
                setForeground(Color.BLACK);
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
