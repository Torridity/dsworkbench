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
        MarkerCell cell = (MarkerCell) value;
        if (!isSelected) {
            if (row % 2 == 0) {
                cell.setBackground(Constants.DS_ROW_B);
            } else {
                cell.setBackground(Constants.DS_ROW_A);
            }
            return cell;
        } else {
            cell.setBackground(SELECT_COLOR);
            return cell;
        }
    }
}
