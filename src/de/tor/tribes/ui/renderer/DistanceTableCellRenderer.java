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
            if (v < markerMin) {
                c.setBackground(Color.GREEN);
            } else if (v > markerMax) {
                c.setBackground(Color.RED);
            }else{
                c.setBackground(Color.YELLOW);
            }
        }
        return c;
    }
}
