/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    public DateCellRenderer() {
        super();
    }

    public DateCellRenderer(String pPattern) {
        this();
    }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : Constants.DATE_FORMAT.format(value));
    }
}
