/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.views.DSWorkbenchMerchantDistibutor.Resource;
import de.tor.tribes.ui.views.DSWorkbenchMerchantDistibutor.Transport;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Torridity
 */
public class TransportCellRenderer extends DefaultTableCellRenderer {

    private DefaultTableCellRenderer renderer = null;
    private NumberFormat nf;
    private List<String> iconsUrls = null;

    public TransportCellRenderer() {
        super();
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        renderer = new DefaultTableCellRenderer();
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
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);


        if (!isSelected) {
            if (row % 2 == 0) {
                label.setBackground(Constants.DS_ROW_B);
            } else {
                label.setBackground(Constants.DS_ROW_A);
            }
        }
        Transport t = (Transport) value;

        Resource woodTransport = t.getSingleTransports().get(0);
        Resource clayTransport = t.getSingleTransports().get(1);
        Resource ironTransport = t.getSingleTransports().get(2);

        StringBuffer text = new StringBuffer();
        text.append("<html>");
        text.append(nf.format(woodTransport.getAmount()));
        text.append(" ");
        text.append("<img src='" + iconsUrls.get(0) + "'/>");
        text.append(nf.format(clayTransport.getAmount()));
        text.append(" ");
        text.append("<img src='" + iconsUrls.get(1) + "'/>");
        text.append(nf.format(ironTransport.getAmount()));
        text.append(" ");
        text.append("<img src='" + iconsUrls.get(2) + "'/>");
        text.append("</html>");
        label.setText(text.toString());
        /* label.setText(nf.format(res.getAmount()));
        if (res.getType() == Resource.Type.WOOD) {
        label.setIcon(woodIcon);
        } else if (res.getType() == Resource.Type.CLAY) {
        label.setIcon(clayIcon);
        } else if (res.getType() == Resource.Type.IRON) {
        label.setIcon(ironIcon);
        }*/
        // label.setIconTextGap(3);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        //  label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
