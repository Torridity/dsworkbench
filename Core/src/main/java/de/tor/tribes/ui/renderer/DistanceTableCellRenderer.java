/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.ui.util.ColorGradientHelper;
import de.tor.tribes.util.DSCalculator;
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

    private UnitHolder unit = UnknownUnit.getSingleton();
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

    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = (JLabel) c;
        double v = (Double) value;
        if (unit.equals(UnknownUnit.getSingleton())) {
            l.setText(nf.format(value));
        } else {
            double speedInMinutes = v * unit.getSpeed();
            l.setText(DSCalculator.formatTimeInMinutes(speedInMinutes));
        }

        Color col = null;
        if (v <= markerMin) {
            col = ColorGradientHelper.getGradientColor(100f, Color.RED, Color.GREEN);
        } else if ((v > markerMin) && (v < markerMax)) {
            double range = markerMax - markerMin;
            double val = v - markerMin;
            col = ColorGradientHelper.getGradientColor((float) (100.0 - (100.0 * val / range)), Color.RED, Color.GREEN);
        } else if (v >= markerMax) {
            col = ColorGradientHelper.getGradientColor(0f, Color.RED, Color.GREEN);
        }
        c.setBackground(col);
        return c;
    }
}
