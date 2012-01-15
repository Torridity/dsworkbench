/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.FightReport;
import de.tor.tribes.ui.components.ReportInfoPanel;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Jejkal
 */
public class ReportWallCataCellRenderer extends DefaultTableRenderer {

    private ReportInfoPanel panel = null;

    public ReportWallCataCellRenderer() {
        super();
        panel = new ReportInfoPanel();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        panel.setBackground(label.getBackground());
        panel.configure((FightReport) value);
        return panel;
    }
}
