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
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Charon
 */
public class MarkerPanelCellRenderer implements TableCellRenderer {

    private Color SELECT_COLOR = new Color(230, 230, 230);
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!isSelected) {
            ((MarkerCell) value).setBackground(Color.WHITE);
            return (MarkerCell) value;
        } else {
            ((MarkerCell) value).setBackground(SELECT_COLOR);
            return (MarkerCell) value;
        }
    }
}