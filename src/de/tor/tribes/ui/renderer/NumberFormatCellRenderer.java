/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class NumberFormatCellRenderer extends DefaultTableCellRenderer {

    private NumberFormat format = NumberFormat.getInstance();

    public NumberFormatCellRenderer() {
        super();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
    }

    public NumberFormatCellRenderer(NumberFormat pCustomFormat) {
        this();
        format = pCustomFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            if (row % 2 == 0) {
                c.setBackground(Constants.DS_ROW_B);
            } else {
                c.setBackground(Constants.DS_ROW_A);
            }
        }
        return c;
    }

    @Override
    public void setValue(Object value) {
        try {
            setText(format.format(value));
        } catch (Exception e) {
            if (value != null) {
                setText(value.toString());
            } else {
                setText("0");
            }
        }
    }
}
