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
    private ImageIcon impossibleIcon = null;
    private ImageIcon failedIcon = null;
    private ImageIcon disabledIcon = null;
    private ImageIcon unknownIcon = null;

    public FarmResultRenderer() {
        super();
        try {
            okIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_green.png"));
            impossibleIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_yellow.png"));
            failedIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_red.png"));
            disabledIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_grey.png"));
            unknownIcon = new ImageIcon(FarmStatusCellRenderer.class.getResource("/res/ui/bullet_ball_empty.png"));
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
                case FAILED:
                    label.setIcon(failedIcon);
                    break;
                case IMPOSSIBLE:
                    label.setIcon(impossibleIcon);
                    break;
                case FARM_INACTIVE:
                    label.setIcon(disabledIcon);
                    break;
                case UNKNOWN:
                    label.setIcon(unknownIcon);
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
