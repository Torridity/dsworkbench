/*
 * AttackerTableModel.java
 * 
 * Created on 25.07.2007, 16:29:29
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.tor.tribes.renderer;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Charon
 */
public class AttackerTypeCellRenderer implements TableCellRenderer {

    public AttackerTypeCellRenderer() {

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return (JComboBox)value;
    }

}
