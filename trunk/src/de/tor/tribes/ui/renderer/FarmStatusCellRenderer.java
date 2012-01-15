/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.ui.ImageManager;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class FarmStatusCellRenderer extends DefaultTableRenderer {

    private ImageIcon readyIcon = null;
    private ImageIcon notSpyedIcon = null;
    private ImageIcon troopsFoundIcon = null;
    private ImageIcon conqueredIcon = null;
    private ImageIcon farmingIcon = null;
    private ImageIcon reportIcon = null;
    private ImageIcon returningIcon = null;

    public FarmStatusCellRenderer() {
        super();
        try {
            readyIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/checkbox.png"));
            notSpyedIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/spy.png"));
            troopsFoundIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/skull.png"));
            conqueredIcon = new ImageIcon("./graphics/icons/warning.png");
            farmingIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/trade_in.png"));
            returningIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/trade_out.png"));
            reportIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/report.png"));
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
            FarmInformation.FARM_STATUS status = (FarmInformation.FARM_STATUS) value;
            switch (status) {
                case NOT_SPYED:
                    label.setIcon(notSpyedIcon);
                    break;
                case TROOPS_FOUND:
                    label.setIcon(troopsFoundIcon);
                    break;
                case CONQUERED:
                    label.setIcon(conqueredIcon);
                    break;
                case FARMING:
                    label.setIcon(farmingIcon);
                    break;
                case RETURNING:
                    label.setIcon(returningIcon);
                    break;
                case REPORT_EXPECTED:
                    label.setIcon(reportIcon);
                    break;
                default:
                    label.setIcon(readyIcon);
            }
        } catch (Exception e) {
            label.setText("?");
            label.setIcon(null);
        }
        return label;
    }
}
