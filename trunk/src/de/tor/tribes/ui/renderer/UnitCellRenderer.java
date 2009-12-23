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
        //   Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //if (bShowCombobox) {
        setModel(new DefaultComboBoxModel(new Object[]{value}));
        setBorder(BorderFactory.createEmptyBorder());
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        setSelectedIndex(0);
        return this;
        /* } else {
        JLabel c = new JLabel();
        if (isSelected) {
        c.setForeground(table.getSelectionForeground());
        c.setBackground(table.getSelectionBackground());
        } else {
        c.setBackground(table.getBackground());
        c.setForeground(table.getForeground());
        }
        try {
        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        if (value == null) {
        //no icon!?
        ((JLabel) c).setText("-");
        ((JLabel) c).setIcon(null);
        } else {
        ((JLabel) c).setText("");
        ((JLabel) c).setIcon(ImageManager.getUnitIcon((UnitHolder) value));
        }
        } catch (Exception e) {
        //cast problem
        }
        return c;
        }*/
    }
}
