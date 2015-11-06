/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
