/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import net.java.dev.colorchooser.ColorChooser;

/**
 *
 * @author Charon
 */
public class ColorChooserCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final ColorChooser chooserComponent = new ColorChooser();

    @Override
    public Object getCellEditorValue() {
        return chooserComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        chooserComponent.setColor(((ColorChooser) value).getColor());
        return chooserComponent;

    }
}
