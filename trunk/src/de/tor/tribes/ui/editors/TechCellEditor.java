/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class TechCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox mEditor = new JComboBox();

    public TechCellEditor(int pTechLevels) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 1; i <= pTechLevels; i++) {
            model.addElement(i);
        }
        mEditor.setModel(model);
    }

    @Override
    public Object getCellEditorValue() {
        return mEditor.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mEditor.setSelectedItem(value);
        if (isSelected) {
            mEditor.setBackground(Constants.DS_BACK);
        } else {
            mEditor.setBackground(Constants.DS_BACK_LIGHT);
        }
        return mEditor;
    }
}
