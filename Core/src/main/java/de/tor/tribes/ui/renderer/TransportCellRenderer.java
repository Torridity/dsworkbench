/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Resource;
import de.tor.tribes.types.Transport;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class TransportCellRenderer extends DefaultTableRenderer {

    private NumberFormat nf;
    private List<String> iconsUrls = null;

    public TransportCellRenderer() {
        super();
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        try {
            iconsUrls = new LinkedList<String>();
            iconsUrls.add(this.getClass().getResource("/res/ui/holz.png").toString());
            iconsUrls.add(this.getClass().getResource("/res/ui/lehm.png").toString());
            iconsUrls.add(this.getClass().getResource("/res/ui/eisen.png").toString());
        } catch (Exception e) {
            iconsUrls = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;

        Transport t = (Transport) value;

        Resource woodTransport = t.getSingleTransports().get(0);
        Resource clayTransport = t.getSingleTransports().get(1);
        Resource ironTransport = t.getSingleTransports().get(2);

        StringBuilder text = new StringBuilder();
        text.append("<html>");
        text.append(nf.format(woodTransport.getAmount()));
        text.append(" ");
        text.append("<img src='").append(iconsUrls.get(0)).append("'/>");
        text.append(nf.format(clayTransport.getAmount()));
        text.append(" ");
        text.append("<img src='").append(iconsUrls.get(1)).append("'/>");
        text.append(nf.format(ironTransport.getAmount()));
        text.append(" ");
        text.append("<img src='").append(iconsUrls.get(2)).append("'/>");
        text.append("</html>");
        label.setText(text.toString());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
