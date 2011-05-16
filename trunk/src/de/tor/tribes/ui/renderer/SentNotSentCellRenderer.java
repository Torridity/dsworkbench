/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class SentNotSentCellRenderer extends DefaultTableRenderer {

    private URL sent = null;
    private URL notSent = null;

    public SentNotSentCellRenderer() {
        super();
        sent = SentNotSentCellRenderer.class.getResource("/res/ui/att_browser.png");
        notSent = SentNotSentCellRenderer.class.getResource("/res/ui/att_browser_unsent.png");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        StringBuilder text = new StringBuilder();
        text.append("<html>");
        if ((Boolean) value) {
            text.append("<img src='").append(sent).append("' width='16' height='16'/>");
        } else {
            text.append("<img src='").append(notSent).append("' width='16' height='16'/>");
        }
        text.append("</html>");

        label.setText(text.toString());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
