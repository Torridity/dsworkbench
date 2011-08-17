/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.TimeSpan;
import de.tor.tribes.util.Constants;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

/**
 *
 * @author Torridity
 */
public class TimeFrameListCellRenderer extends JLabel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object pValue, int pIndex, boolean pSelected, boolean pHasFocus) {
        try {
            setOpaque(true);
            TimeSpan span = (TimeSpan) pValue;
            setBorder(null);
            switch (span.getDirection()) {
                case SEND:
                    setIcon(new ImageIcon(TimeFrameListCellRenderer.class.getResource("/res/ui/move_out.png")));
                    break;
                case ARRIVE:
                    setIcon(new ImageIcon(TimeFrameListCellRenderer.class.getResource("/res/ui/move_in.png")));
                    break;
                case NONE:
                    setIcon(null);
                    setForeground(Constants.DS_BACK_LIGHT);
                    setBackground(Constants.DS_BACK_LIGHT);
                    setSize(list.getWidth(), 2);
                    /// setBorder(LineBorder.createGrayLineBorder());
                    setText(" ");
                    return this;
            }
            if (pSelected) {
                setForeground(list.getSelectionForeground());
                super.setBackground(list.getSelectionBackground());
            } else {
                if (!span.isValid()) {
                    setBackground(Color.red);
                    setForeground(Color.white);
                    setToolTipText(span.getValidityInfo());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                    setToolTipText(null);
                }
            }
            setText(span.toString());
        } catch (Exception e) {
            //cast problem
        }

        return this;

    }
}
