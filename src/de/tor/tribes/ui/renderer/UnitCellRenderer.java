/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Torridity
 */
//public class UnitCellRenderer extends JComboBox implements TableCellRenderer {
public class UnitCellRenderer extends JLabel implements TableCellRenderer {

    public UnitCellRenderer() {
        super();
        // setRenderer(new UnitListCellRenderer());

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //setModel(new DefaultComboBoxModel(new Object[]{value}));
        UnitHolder unit = (UnitHolder) value;
        setText("");
        setHorizontalAlignment(SwingConstants.CENTER);
        setIcon(ImageManager.getUnitIcon(unit));
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


        // setSelectedItem(value);
        return this;
    }
}
