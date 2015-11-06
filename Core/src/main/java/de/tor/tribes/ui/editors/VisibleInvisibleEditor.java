/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class VisibleInvisibleEditor extends AbstractCellEditor implements TableCellEditor {

    private JCheckBox editorComponent = null;

    public VisibleInvisibleEditor() {
        editorComponent = new JCheckBox();
        try {
            editorComponent.setIcon(new ImageIcon(this.getClass().getResource("/res/ui/eye.png")));
            editorComponent.setSelectedIcon(new ImageIcon(this.getClass().getResource("/res/ui/eye_forbidden.png")));
            editorComponent.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (Exception e) {
            editorComponent.setIcon(null);
            editorComponent.setSelectedIcon(null);
        }
        editorComponent.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                stopCellEditing();
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return editorComponent.isSelected();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editorComponent.setSelected((Boolean) value);
        editorComponent.setText(null);
        return editorComponent;
    }
}
