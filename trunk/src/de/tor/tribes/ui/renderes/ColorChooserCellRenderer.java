/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.renderes;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import net.java.dev.colorchooser.ColorChooser;

/**
 *
 * @author Charon
 */
public class ColorChooserCellRenderer implements TableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return (ColorChooser)value;
    }

}
