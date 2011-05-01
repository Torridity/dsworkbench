/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.FightReport;
import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Jejkal
 */
public class ReportWallCataCellRenderer extends DefaultTableRenderer {

    private List<String> iconsUrls = null;

    public ReportWallCataCellRenderer() {
        super();
        try {
            iconsUrls = new LinkedList<String>();
            iconsUrls.add(new File("./graphics/icons/wall.png").toURI().toURL().toString());
            iconsUrls.add(new File("./graphics/icons/cata.png").toURI().toURL().toString());
            iconsUrls.add(new File("./graphics/icons/snob.png").toURI().toURL().toString());
        } catch (Exception e) {
            iconsUrls = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        try {
            label.setHorizontalAlignment(SwingConstants.CENTER);
            FightReport r = (FightReport) value;
            byte v = r.getVillageEffects();
            StringBuilder text = new StringBuilder();
            text.append("<html>");
            StringBuilder tooltip = new StringBuilder();
            tooltip.append("<html>");
            boolean hasTooltip = false;
            if ((v & 1) > 0) {
                text.append("<img src='").append(iconsUrls.get(0)).append("'/>");
                hasTooltip = true;
                tooltip.append("Wall beschädigt von Level ").append(r.getWallBefore()).append(" auf Level ").append(r.getWallAfter()).append("<BR/>");
            }
            if ((v & 2) > 0) {
                text.append("<img src='").append(iconsUrls.get(1)).append("'/>");
                hasTooltip = true;
                tooltip.append(r.getAimedBuilding()).append(" beschädigt von Level ").append(r.getBuildingBefore()).append(" auf Level ").append(r.getBuildingAfter()).append("<BR/>");
            }
            if ((v & 4) > 0) {
                text.append("<img src='").append(iconsUrls.get(2)).append("'/>");
                hasTooltip = true;
                tooltip.append("Zustimmung gesenkt von ").append(r.getAcceptanceBefore()).append(" auf ").append(r.getAcceptanceAfter());
            }


            text.append("</html>");
            tooltip.append("</html>");

            label.setText(text.toString());
            if (hasTooltip) {
                label.setToolTipText(tooltip.toString());
            }
        } catch (Exception e) {
            //cast problem
        }
        return label;
    }
}
