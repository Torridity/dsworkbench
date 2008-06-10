/*
 * AttackerCellEditor.java
 *
 * Created on 25.07.2007, 16:33:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.tor.tribes.editor;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class AttackerCellEditor implements TableCellEditor{
    
    private DefaultCellEditor mDefaultEditor = null;
    private JComboBox mSelection = null;
    
    public AttackerCellEditor() {
        mSelection = new JComboBox(new Object[]{"test", "test1"});
        mDefaultEditor = new DefaultCellEditor(mSelection);
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return mDefaultEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    public Object getCellEditorValue() {
        return mDefaultEditor.getCellEditorValue();
    }
    
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }
    
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    
    public boolean stopCellEditing() {
        return mDefaultEditor.stopCellEditing();
    }
    
    public void cancelCellEditing() {
        mDefaultEditor.cancelCellEditing();
    }
    
    public void addCellEditorListener(CellEditorListener l) {
        
    }
    
    public void removeCellEditorListener(CellEditorListener l) {
        
    }
    
}
