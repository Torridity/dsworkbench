/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Torridity
 */
public class UnitCellRenderer extends JComboBox implements TableCellRenderer {

    public UnitCellRenderer() {
        super();
        setRenderer(new UnitListCellRenderer());
        setBackground(Constants.DS_BACK);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setModel(new DefaultComboBoxModel(new Object[]{value}));
        setBorder(BorderFactory.createEmptyBorder());
        if (!isSelected) {
            if (row % 2 == 0) {
                setBackground(Constants.DS_ROW_B);
            } else {
                setBackground(Constants.DS_ROW_A);
            }
        } else {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }

        /*if (isSelected) {
        setForeground(table.getSelectionForeground());
        super.setBackground(table.getSelectionBackground());
        } else {
        setBackground(table.getBackground());
        setForeground(table.getForeground());
        }*/
        setSelectedItem(value);
        return this;
    }
}
