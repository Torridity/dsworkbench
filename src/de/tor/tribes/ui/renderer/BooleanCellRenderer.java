/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Torridity
 */
public class BooleanCellRenderer extends JCheckBox implements TableCellRenderer {

    public BooleanCellRenderer() {
        super();
        // setRenderer(new UnitListCellRenderer());

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //setModel(new DefaultComboBoxModel(new Object[]{value}));
        setText("");
        setHorizontalAlignment(SwingConstants.CENTER);
        // setBorder(BorderFactory.createEmptyBorder());
        setOpaque(true);
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

        setSelected((Boolean) value);
        // setSelectedItem(value);
        return this;
    }
}
