/*
 * MarkerListCellRenderer.java
 *
 * Created on 07.10.2007, 14:21:15
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.renderes;

import de.tor.tribes.ui.MarkerPanel;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Charon
 */
public class MarkerListCellRenderer implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof MarkerPanel) {
            MarkerPanel panel = (MarkerPanel) value;
            panel.setSelected(isSelected);
            return panel;
        }
        return new JLabel(value.toString());
    }
}