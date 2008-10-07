/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.tag.Tag;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Jejkal
 */
public class TagCellRenderer implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public TagCellRenderer() {
       // mRenderer = new JLabel();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        try {
            Tag t = (Tag) value;
            defaultRenderer.setText(t.getName());
            defaultRenderer.setIcon(new ImageIcon(t.getTagIcon()));
        } catch (Exception e) {
            defaultRenderer.setText(value.toString());
            defaultRenderer.setIcon(new ImageIcon(this.getClass().getResource("/res/forbidden.gif")));
        }
        if (isSelected) {
            defaultRenderer.setBackground(de.tor.tribes.util.Constants.DS_BACK);
        } else {
            defaultRenderer.setBackground(de.tor.tribes.util.Constants.DS_BACK_LIGHT);
        }
        return defaultRenderer;
    }
}
