/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.ui.ImageManager;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class TagMapMarkerRenderer implements TableCellRenderer {

    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        TagMapMarker tagMarker = (TagMapMarker) value;
        Color selectColor = tagMarker.getTagColor();
        if (selectColor == null) {
            selectColor = c.getBackground();
        } else {
            if (isSelected) {
                selectColor = selectColor.darker();
            }
        }

        c.setBackground(selectColor);
        ((JLabel) c).setText("");
        if (tagMarker.getTagIcon() >= 0) {
            ((JLabel) c).setIcon(ImageManager.getUnitIcon(tagMarker.getTagIcon(), false));
        }
        return c;
    }
}
