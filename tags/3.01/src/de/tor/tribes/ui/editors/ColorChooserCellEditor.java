/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import net.java.dev.colorchooser.ColorChooser;

/**
 *
 * @author Jejkal
 */
public class ColorChooserCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final ColorChooser chooserComponent = new ColorChooser();

    public ColorChooserCellEditor(ActionListener pListener) {
        chooserComponent.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
        chooserComponent.addActionListener(pListener);
    }

    @Override
    public Object getCellEditorValue() {
        return chooserComponent.getColor();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        chooserComponent.setColor((Color) value);
        return chooserComponent;
    }
}
