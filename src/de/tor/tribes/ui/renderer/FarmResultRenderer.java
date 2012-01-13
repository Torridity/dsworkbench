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
    private ImageIcon noSourceByCarryCapacityIcon = null;
    private ImageIcon noSourceByMinHaulIcon = null;
    private ImageIcon noSourceByRangeIcon = null;
    private ImageIcon noTroopsIcon = null;
    private ImageIcon browserFailedIcon = null;

    public FarmResultRenderer() {
        super();
        try {
            okIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/farm_result_ok.png"));
            noSourceByCarryCapacityIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/farm_result_net.png"));
            noSourceByMinHaulIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/farm_result_mhnr.png"));
            noSourceByRangeIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/farm_result_nvir.png"));
            noTroopsIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/farm_result_nt.png"));
            browserFailedIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/farm_result_ft2b.png"));
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
                case NO_ADEQUATE_SOURCE_BY_RANGE:
                    label.setIcon(noSourceByRangeIcon);
                    break;
                case NO_ADEQUATE_SOURCE_BY_NEEDED_TROOPS:
                    label.setIcon(noSourceByCarryCapacityIcon);
                    break;
                case NO_ADEQUATE_SOURCE_BY_MIN_HAUL:
                    label.setIcon(noSourceByMinHaulIcon);
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
