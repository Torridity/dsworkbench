/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    private SimpleDateFormat specialFormat = null;

    public DateCellRenderer() {
        super();
    }

    public DateCellRenderer(String pPattern) {
        this();
        specialFormat = new SimpleDateFormat(pPattern);
    }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : ((specialFormat != null) ? specialFormat.format(value) : Constants.DATE_FORMAT.format(value)));
    }
}
