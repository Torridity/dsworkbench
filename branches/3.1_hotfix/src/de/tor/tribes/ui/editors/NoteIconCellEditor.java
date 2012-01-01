/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.renderer.NoteIconListCellRenderer;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Torridity
 */
public class NoteIconCellEditor extends AbstractCellEditor implements TableCellEditor {

    public enum ICON_TYPE {

        NOTE, MAP
    }
    private JComboBox mEditor = new JComboBox();
    private ICON_TYPE type = ICON_TYPE.NOTE;

    public NoteIconCellEditor(ICON_TYPE pType) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        type = pType;

        switch (type) {
            case NOTE: {
                for (int i = -1; i <= ImageManager.NOTE_SYMBOL_WALL; i++) {
                    model.addElement(i);
                }
                break;
            }
            case MAP: {
                for (int i = -1; i <= ImageManager.ID_NOTE_ICON_13; i++) {
                    model.addElement(i);
                }
                break;
            }
        }


        mEditor.setModel(model);
        mEditor.setRenderer(new NoteIconListCellRenderer(type));
        mEditor.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stopCellEditing();
                }
            }
        });
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        return true;
    }

    @Override
    public Object getCellEditorValue() {
        return mEditor.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mEditor.setSelectedItem(value);
        return mEditor;
    }
}
