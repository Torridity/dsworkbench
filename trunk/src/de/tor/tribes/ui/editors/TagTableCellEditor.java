/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.Tag;
import de.tor.tribes.ui.renderer.TagCellRenderer;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import de.tor.tribes.util.Constants;

/**
 *
 * @author Jejkal
 */
public class TagTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private TagCellRenderer mRenderer = new TagCellRenderer();

    @Override
    public Object getCellEditorValue() {
        return mRenderer.getValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mRenderer.setValue((Tag) value);
        if (isSelected) {
            mRenderer.setBackground(Constants.DS_BACK);
        } else {
            mRenderer.setBackground(Constants.DS_BACK_LIGHT);
        }
        return mRenderer;
    }
}
