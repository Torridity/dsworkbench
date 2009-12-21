/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class FakeCellRenderer extends DefaultTableCellRenderer {

    private DefaultTableCellRenderer renderer = null;

    public FakeCellRenderer() {
        super();
        renderer = new DefaultTableCellRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
               
        ((JCheckBox)c).setSelectedIcon(new ImageIcon(this.getClass().getResource("/res/fake.png")));
        ((JCheckBox)c).setIcon(new ImageIcon(this.getClass().getResource("/res/no_fake.png")));
        return c;
    }
}
