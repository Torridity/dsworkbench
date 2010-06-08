/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @TODO (DIFF) Smoother color gradient, expired values are stroked
 * @author Jejkal
 */
public class ColoredDateCellRenderer implements TableCellRenderer {

    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
    private DefaultTableCellRenderer coloredRenderer = null;
    private DefaultTableCellRenderer defaultRenderer = null;
    private final int MINUTE = (1000 * 60);

    public ColoredDateCellRenderer() {
        //super();
        coloredRenderer = new DefaultTableCellRenderer();
        defaultRenderer = new DefaultTableCellRenderer();
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        }
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
        long diff = t - now;
        long five_minutes = 5 * MINUTE;
        long ten_minutes = 10 * MINUTE;

        if (t <= now) {
            //value is expired, stroke result
            JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                if (row % 2 == 0) {
                    label.setBackground(Constants.DS_ROW_B);
                } else {
                    label.setBackground(Constants.DS_ROW_A);
                }
            }
            label.setText("<html><s>" + specialFormat.format(d) + "</s></html>");
            return label;
        } else if (diff <= ten_minutes && diff > five_minutes) {
            float ratio = (float) (diff - five_minutes) / (float) five_minutes;
            Color c1 = Color.YELLOW;
            Color c2 = Color.GREEN;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            Color time_color = new Color(red, green, blue);
            renderComponent.setBackground(time_color);
        } else if (diff <= five_minutes) {
            float ratio = (float) diff / (float) five_minutes;
            Color c1 = Color.RED;
            Color c2 = Color.YELLOW;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            Color time_color = new Color(red, green, blue);
            renderComponent.setBackground(time_color);
        } else {
            //default renderer and color
            JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    label.setBackground(Constants.DS_ROW_B);
                } else {
                    label.setBackground(Constants.DS_ROW_A);
                }
            }
            label.setText(specialFormat.format((Date) value));
            return label;
        }
        return c;
    }
}
