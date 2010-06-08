/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ServerSettings;
import java.awt.Component;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");

    public DateCellRenderer() {
        super();
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        } else {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
        }
    }

    public DateCellRenderer(String pPattern) {
        this();
        specialFormat = new SimpleDateFormat(pPattern);
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
        setText((value == null) ? "" : specialFormat.format(value));
    }
}
