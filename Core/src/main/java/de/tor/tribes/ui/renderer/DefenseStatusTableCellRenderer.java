/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.DefenseInformation.DEFENSE_STATUS;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Torridity
 */
public class DefenseStatusTableCellRenderer extends DefaultTableRenderer {
    
    private ImageIcon unknown = null;
    private ImageIcon fine = null;
    private ImageIcon save = null;
    private ImageIcon dangerous = null;
    
    public DefenseStatusTableCellRenderer() {
        unknown = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_grey.png"));
        fine = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_yellow.png"));
        save = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_green.png"));
        dangerous = new ImageIcon(TendencyTableCellRenderer.class.getResource("/res/ui/bullet_ball_red.png"));
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        DEFENSE_STATUS val = (DEFENSE_STATUS) value;
        switch (val) {
            case DANGEROUS:
                label.setIcon(dangerous);
                break;
            case FINE:
                label.setIcon(fine);
                break;
            case SAVE:
                label.setIcon(save);
                break;
            default:
                label.setIcon(unknown);
                break;
        }
        label.setText("");
        return label;
    }
}
