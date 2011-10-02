/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.editors;

import de.tor.tribes.types.TagMapMarker;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Jejkal
 */
public class TagMapMarkerCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final TagMapMarkerEditorImpl editor = new TagMapMarkerEditorImpl();

    @Override
    public Object getCellEditorValue() {
        TagMapMarker m = new TagMapMarker();
        m.setTagColor(editor.getColor());
        m.setTagIcon(editor.getIcon());
        return m;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        TagMapMarker marker = (TagMapMarker) value;
        editor.setColor(marker.getTagColor());
        editor.setIcon(marker.getTagIcon());
        return editor;
    }
}
