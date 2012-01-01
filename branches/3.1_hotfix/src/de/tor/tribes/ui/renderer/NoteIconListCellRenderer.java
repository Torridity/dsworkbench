/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.editors.NoteIconCellEditor.ICON_TYPE;
import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;

/**
 *
 * @author Torridity
 */
public class NoteIconListCellRenderer extends DefaultListCellRenderer {

    private ICON_TYPE type = ICON_TYPE.NOTE;

    public NoteIconListCellRenderer(ICON_TYPE pType) {
        type = pType;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        try {
            JLabel label = ((JLabel) c);
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
        return c;
    }
}
