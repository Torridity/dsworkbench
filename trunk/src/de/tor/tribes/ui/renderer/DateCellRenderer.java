/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.ServerSettings;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");

    public DateCellRenderer() {
        super();
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        }
    }

    public DateCellRenderer(String pPattern) {
        this();
        specialFormat = new SimpleDateFormat(pPattern);
    }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : specialFormat.format(value));
    }
}
