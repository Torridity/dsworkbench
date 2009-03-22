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
 *
 * @author Jejkal
 */
public class ColoredDateCellRenderer implements TableCellRenderer {

    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
    private DefaultTableCellRenderer coloredRenderer = null;
    private DefaultTableCellRenderer defaultRenderer = null;
    private final int TEN_MINUTES = (1000 * 60 * 10);
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
        if (column != 3) {
            Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            ((JLabel) c).setText(specialFormat.format((Date) value));
            return c;
        }

        Component c = coloredRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        JLabel renderComponent = ((JLabel) c);
        Date d = (Date) value;
        long t = d.getTime();
        long now = System.currentTimeMillis();
        renderComponent.setText(specialFormat.format(d));

        if (t <= now) {
            renderComponent.setBackground(Color.RED);
        } else if (t - TEN_MINUTES > now) {
            //more than 10 minutes in past, do nothing
            c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            ((JLabel) c).setText(specialFormat.format((Date) value));
            return c;
        } else {
            //do gradient calculation
            long diff = t - now;
            float posv = 100.0f * (float) diff / (float) TEN_MINUTES;
            posv = (int) ((int) posv / 10) * 10;
            posv /= 100;
            int r = (int) Math.rint((float) LAST_SEGMENT.getRed() * (1.0f - posv) + (float) Color.YELLOW.getRed() * posv);
            int g = (int) Math.rint((float) LAST_SEGMENT.getGreen() * (1.0f - posv) + (float) Color.YELLOW.getGreen() * posv);
            int b = (int) Math.rint((float) LAST_SEGMENT.getBlue() * (1.0f - posv) + (float) Color.YELLOW.getBlue() * posv);
            renderComponent.setBackground(new Color(r, g, b));
        }
        return c;
    }
}
