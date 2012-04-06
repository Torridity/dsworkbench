/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.VillageMerchantInfo;
import de.tor.tribes.util.Constants;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class TradeDirectionCellRenderer extends DefaultTableCellRenderer {

    private DefaultTableCellRenderer renderer = null;
    private ImageIcon tradeBoth;
    private ImageIcon tradeIn;
    private ImageIcon tradeOut;

    public TradeDirectionCellRenderer() {
        super();
        renderer = new DefaultTableCellRenderer();
        try {
            tradeBoth = new ImageIcon(this.getClass().getResource("/res/ui/trade_both.png"));
            tradeIn = new ImageIcon(this.getClass().getResource("/res/ui/trade_in.png"));
            tradeOut = new ImageIcon(this.getClass().getResource("/res/ui/trade_out.png"));
        } catch (Exception e) {
            e.printStackTrace();
            tradeBoth = null;
            tradeIn = null;
            tradeOut = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        label.setText("");

        if (!isSelected) {
            if (row % 2 == 0) {
                label.setBackground(Constants.DS_ROW_B);
            } else {
                label.setBackground(Constants.DS_ROW_A);
            }
        }
        VillageMerchantInfo.Direction dir = (VillageMerchantInfo.Direction) value;

        if (dir == VillageMerchantInfo.Direction.BOTH) {
            label.setIcon(tradeBoth);
        } else if (dir == VillageMerchantInfo.Direction.INCOMING) {
            label.setIcon(tradeIn);
        } else if (dir == VillageMerchantInfo.Direction.OUTGOING) {
            label.setIcon(tradeOut);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
