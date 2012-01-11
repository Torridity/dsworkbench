/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.FarmInformation;
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
public class FarmResultRenderer extends DefaultTableRenderer {

    private ImageIcon okIcon = null;
    private ImageIcon notEnoughResourcesIcon = null;
    private ImageIcon noSourceByTroopsIcon = null;
    private ImageIcon noSourceByResourcesIcon = null;
    private ImageIcon noTroopsIcon = null;
    private ImageIcon browserFailedIcon = null;

    public FarmResultRenderer() {
        super();
        try {
            //@TODO Create icons
            okIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/checkbox.png"));
            notEnoughResourcesIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/spy.png"));
            noSourceByTroopsIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/skull.png"));
            noSourceByResourcesIcon = new ImageIcon("./graphics/icons/warning.png");
            noTroopsIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/trade_in.png"));
            browserFailedIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/trade_out.png"));
        } catch (Exception e) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = ((JLabel) c);
        try {
            label.setText("");

            label.setHorizontalAlignment(SwingConstants.CENTER);
            FarmInformation.FARM_RESULT status = (FarmInformation.FARM_RESULT) value;
            switch (status) {
                case FAILED_OPEN_BROWSER:
                    label.setIcon(browserFailedIcon);
                    break;
                case NOT_ENOUGH_RESOURCES:
                    label.setIcon(notEnoughResourcesIcon);
                    break;
                case NO_ADEQUATE_SOURCE_BY_RANGE:
                    label.setIcon(noSourceByResourcesIcon);
                    break;
                case NO_ADEQUATE_SOURCE_BY_TROOPS:
                    label.setIcon(noSourceByTroopsIcon);
                    break;
                case NO_TROOPS:
                    label.setIcon(noTroopsIcon);
                    break;
                default:
                    label.setIcon(okIcon);
            }
        } catch (Exception e) {
            label.setText("?");
            label.setIcon(null);
        }
        return label;
    }
}
