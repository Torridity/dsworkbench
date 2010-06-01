/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author Jejkal
 */
public class ColoredDateCellRenderer implements TableCellRenderer {

    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
    private DefaultTableCellRenderer coloredRenderer = null;
    private DefaultTableCellRenderer defaultRenderer = null;
    private final int MINUTE = (1000 * 60);
    private final Color LAST_SEGMENT = new Color(255, 100, 0);

    public ColoredDateCellRenderer() {
        //super();
        coloredRenderer = new DefaultTableCellRenderer();
        defaultRenderer = new DefaultTableCellRenderer();
    }

    public ColoredDateCellRenderer(String pPattern) {
        this();
        specialFormat = new SimpleDateFormat(pPattern);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = coloredRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        JLabel renderComponent = ((JLabel) c);
        Date d = (Date) value;
        long t = d.getTime();
        long now = System.currentTimeMillis();
        renderComponent.setText(specialFormat.format(d));

        if (t <= now) {
            renderComponent.setBackground(Color.DARK_GRAY);
        } else if (t - now <= 1 * MINUTE) {
            renderComponent.setBackground(Color.RED);
        } else if (t - now <= 3 * MINUTE) {
            renderComponent.setBackground(new Color(255, 125, 0));
        } else if (t - now <= 5 * MINUTE) {
            renderComponent.setBackground(Color.YELLOW);
        } else if (t - now <= 10 * MINUTE) {
            renderComponent.setBackground(Color.GREEN);
        } else {
            //default color
            c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            ((JLabel) c).setText(specialFormat.format((Date) value));
            return c;
        }
        return c;
    }
}
