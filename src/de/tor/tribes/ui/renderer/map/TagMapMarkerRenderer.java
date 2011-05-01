/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.types.TagMapMarker;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.editors.TagMapMarkerEditorImpl;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class TagMapMarkerRenderer implements TableCellRenderer {

    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    final private ImageIcon no_tag = new ImageIcon(TagMapMarkerEditorImpl.class.getResource("/res/remove.gif"));

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        TagMapMarker tagMarker = (TagMapMarker) value;
        Color selectColor = tagMarker.getTagColor();
        if (selectColor == null) {
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Constants.DS_ROW_B);
                } else {
                    c.setBackground(Constants.DS_ROW_A);
                }
            }
        } else {
            if (isSelected) {
                selectColor = selectColor.darker();
            }
            c.setBackground(selectColor);
        }

        ((JLabel) c).setText("");
        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        if (tagMarker.getTagIcon() >= 0) {
            ((JLabel) c).setIcon(ImageManager.getUnitIcon(tagMarker.getTagIcon(), false));
        } else {
            ((JLabel) c).setIcon(no_tag);
        }
        return c;
    }
}
