/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    SimpleDateFormat formatter;
    private String sPattern = "dd.MM.yy HH:mm:ss";

    public DateCellRenderer() {
        super();
    }

    public DateCellRenderer(String pPattern) {
        this();
        sPattern = pPattern;
    }

    @Override
    public void setValue(Object value) {
        if (formatter == null) {
            formatter = new SimpleDateFormat(sPattern);
        }
        setText((value == null) ? "" : formatter.format(value));
    }
}
