/*
 * MarkerListCellRenderer.java
 *
 * Created on 07.10.2007, 14:21:15
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.MarkerCell;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Charon
 */
public class MarkerPanelCellRenderer extends DefaultTableRenderer {

    private Color SELECT_COLOR = new Color(230, 230, 230);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = (JLabel) c;
        MarkerCell cell = (MarkerCell) value;
        cell.setBackground(l.getBackground());
        cell.setForeground(l.getForeground());
        return cell;
    }
}
