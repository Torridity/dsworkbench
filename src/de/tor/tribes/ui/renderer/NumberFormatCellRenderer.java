/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.text.NumberFormat;
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
    public void setValue(Object value) {
        try {
            setText(format.format(value));
        } catch (Exception e) {
            if(value != null){
            setText(value.toString());
            }else{
                setText("0");
            }
        }
    }
}
