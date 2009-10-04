/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.text.NumberFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Charon
 */
public class DistanceTableCellRenderer extends DefaultTableCellRenderer {

    private NumberFormat nf = NumberFormat.getInstance();
    private double markerMin = Double.MIN_VALUE;

    public DistanceTableCellRenderer() {
        super();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        markerMin = 11;
    }
    
    public void setMarkerMin(double pValue) {
        markerMin = pValue;
    }

    @Override
    public void setValue(Object value) {
        Double v = (Double) value;
        setText(nf.format(value));
        if (v < markerMin) {
            setBackground(Color.GREEN);
        }else{
            setBackground(Color.WHITE);
        }
    }
}
