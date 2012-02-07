/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Charon
 */
public class UnitTableHeaderRenderer extends DefaultTableCellRenderer {

    private DefaultTableHeaderRenderer defaultRenderer = null;

    public UnitTableHeaderRenderer() {
        defaultRenderer = new DefaultTableHeaderRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        UnitHolder unit = DataHolder.getSingleton().getUnitByPlainName((String) value);
        JLabel result = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (unit.equals(UnknownUnit.getSingleton())) {
            return result;
        }
        result.setIcon(ImageManager.getUnitIcon(DataHolder.getSingleton().getUnitByPlainName((String) value)));
        result.setText("");
        return result;

    }
}
