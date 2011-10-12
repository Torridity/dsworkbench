/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Charon
 */
public class DistanceTableCellRenderer implements TableCellRenderer {

    private NumberFormat nf = NumberFormat.getInstance();
    private double markerMin = Double.MIN_VALUE;
    private double markerMax = Double.MAX_VALUE;

    public DistanceTableCellRenderer() {
        super();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        markerMin = 10;
        markerMax = 20;
    }

    public void setMarkerMin(double pValue) {
        markerMin = pValue;
    }

    public void setMarkerMax(double pValue) {
        markerMax = pValue;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = (JLabel) c;
        l.setText(nf.format(value));
        double v = (Double) value;

        if (!isSelected) {
            if (v <= markerMin) {
                c.setBackground(Color.GREEN);
            } else if (v >= markerMax) {
                c.setBackground(Color.RED);
            } else if ((v > markerMin) && (v < markerMax)) {
                double diff = markerMax - markerMin;
                float ratio = 0;
                if (diff > 0) {
                    ratio = (float) ((v - markerMin) / (markerMax - markerMin));
                }
                Color c1 = Color.YELLOW;
                Color c2 = Color.RED;
                int red = (int) Math.rint(c2.getRed() * ratio + c1.getRed() * (1f - ratio));
                int green = (int) Math.rint(c2.getGreen() * ratio + c1.getGreen() * (1f - ratio));
                int blue = (int) Math.rint(c2.getBlue() * ratio + c1.getBlue() * (1f - ratio));

                red = (red < 0) ? 0 : red;
                green = (green < 0) ? 0 : green;
                blue = (blue < 0) ? 0 : blue;
                red = (red > 255) ? 255 : red;
                green = (green > 255) ? 255 : green;
                blue = (blue > 255) ? 255 : blue;
                c.setBackground(new Color(red, green, blue));
            }
        }
        return c;
    }
}
