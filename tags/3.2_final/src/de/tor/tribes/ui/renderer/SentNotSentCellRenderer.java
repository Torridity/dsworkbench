/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class SentNotSentCellRenderer extends DefaultTableRenderer {

    private ImageIcon sent = null;
    private ImageIcon notSent = null;

    public SentNotSentCellRenderer() {
        super();
        try {
            sent = new ImageIcon(SentNotSentCellRenderer.class.getResource("/res/ui/sent_small.gif"));
            notSent = new ImageIcon(SentNotSentCellRenderer.class.getResource("/res/ui/unsent_small.gif"));
        } catch (Exception e) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        label.setText(null);
        if ((Boolean) value) {
            label.setIcon(sent);
        } else {
            label.setIcon(notSent);
        }
        /* StringBuilder text = new StringBuilder();
        text.append("<html>");
        if ((Boolean) value) {
        text.append("<img src='").append(sent).append("' width='16' height='16'/>");
        } else {
        text.append("<img src='").append(notSent).append("' width='16' height='16'/>");
        }
        text.append("</html>");
        
        label.setText(text.toString());*/
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
