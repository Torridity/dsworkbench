/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class NoteIconCellRenderer extends DefaultTableRenderer {

    public enum ICON_TYPE {

        NOTE, MAP
    }
    private ICON_TYPE type = ICON_TYPE.NOTE;

    public NoteIconCellRenderer(ICON_TYPE pType) {
        type = pType;

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        try {
            label.setText("");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (type.equals(ICON_TYPE.NOTE) && ((Integer) value) == -1) {
                BufferedImage symbol = ImageManager.getNoteIcon(-1);
                label.setIcon(new ImageIcon(symbol.getScaledInstance(18, 18, BufferedImage.SCALE_FAST)));

            } else {
                BufferedImage symbol = (type.equals(ICON_TYPE.NOTE)) ? ImageManager.getNoteSymbol((Integer) value) : ImageManager.getNoteIcon((Integer) value);
                label.setIcon(new ImageIcon(symbol.getScaledInstance(18, 18, BufferedImage.SCALE_FAST)));
            }
        } catch (Exception e) {
        }
        return label;
    }
}
