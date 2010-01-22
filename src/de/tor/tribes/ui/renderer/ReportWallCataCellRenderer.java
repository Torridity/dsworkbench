/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class ReportWallCataCellRenderer implements TableCellRenderer {

    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    private List<ImageIcon> icons = null;

    public ReportWallCataCellRenderer() {
        try {
            icons = new LinkedList<ImageIcon>();
            icons.add(new ImageIcon("./graphics/icons/wall.png"));
            icons.add(new ImageIcon("./graphics/icons/cata.png"));
            icons.add(new ImageIcon("./graphics/icons/snob.png"));
        } catch (Exception e) {
            icons = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = new JLabel();
        l.setForeground(c.getForeground());
        l.setBackground(c.getBackground());
        if (hasFocus) {
            l.requestFocus();
        }
        try {
            l.setHorizontalAlignment(SwingConstants.CENTER);
            int icon = -1;
            Boolean val = (Boolean) value;
            if (column == table.getColumnCount() - 1) {
                icon = 2;
            } else if (column == table.getColumnCount() - 2) {
                icon = 1;
            } else if (column == table.getColumnCount() - 3) {
                icon = 0;
            }

            if (icon == -1) {
                //no icon!?
                l.setText("-");
                l.setIcon(null);
            } else {
                if (val) {
                    l.setText("");
                    l.setIcon(icons.get(icon));
                } else {
                    l.setText("-");
                    l.setIcon(null);
                }
            }
        } catch (Exception e) {
            //cast problem
            //  e.printStackTrace();
        }
        return l;
    }
}
