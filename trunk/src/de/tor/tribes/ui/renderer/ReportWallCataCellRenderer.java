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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Jejkal
 */
public class ReportWallCataCellRenderer implements TableCellRenderer {

    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    private List<String> iconsUrls = null;

    public ReportWallCataCellRenderer() {
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
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel l = new JLabel();
        l.setOpaque(true);
        l.setForeground(c.getForeground());
        l.setBackground(c.getBackground());
        if (hasFocus) {
            l.requestFocus();
        }
        try {
            l.setHorizontalAlignment(SwingConstants.CENTER);
            /* int icon = -1;
            Boolean val = (Boolean) value;
            if (column == table.getColumnCount() - 1) {
            icon = 2;
            } else if (column == table.getColumnCount() - 2) {
            icon = 1;
            } else if (column == table.getColumnCount() - 3) {
            icon = 0;
            }

            if (icon == -1) {
            //no icon!?
            l.setText("-");
            l.setIcon(null);
            } else {
            if (val) {
            l.setText("");
            l.setIcon(icons.get(icon));
            } else {
            l.setText("-");
            l.setIcon(null);
            }
            }*/

          
            byte v = (Byte) value;
            StringBuffer text = new StringBuffer();
            text.append("<html>");
            StringBuffer tooltip = new StringBuffer();
            tooltip.append("<html>");
            FightReport report = null;
            try {
                report = (FightReport) table.getValueAt(row, 0);
            } catch (Exception e) {
                report = null;
            }
            boolean hasTooltip = false;
            if (report == null) {
                if ((v & 1) > 0) {
                    text.append("<img src='" + iconsUrls.get(0) + "'/>");
                    hasTooltip = true;
                    tooltip.append("Wall beschädigt<BR/>");
                }
                if ((v & 2) > 0) {
                    text.append("<img src='" + iconsUrls.get(1) + "'/>");
                    hasTooltip = true;
                    tooltip.append("Gebäude beschädigt<BR/>");
                }
                if ((v & 4) > 0) {
                    text.append("<img src='" + iconsUrls.get(2) + "'/>");
                    hasTooltip = true;
                    tooltip.append("Geadelt");
                }
            } else {
                if ((v & 1) > 0) {
                    text.append("<img src='" + iconsUrls.get(0) + "'/>");
                    hasTooltip = true;
                    tooltip.append("Wall beschädigt von Level " + report.getWallBefore() + " auf Level " + report.getWallAfter() + "<BR/>");
                }
                if ((v & 2) > 0) {
                    text.append("<img src='" + iconsUrls.get(1) + "'/>");
                    hasTooltip = true;
                    tooltip.append(report.getAimedBuilding() + " beschädigt von Level " + report.getBuildingBefore() + " auf Level " + report.getBuildingAfter() + "<BR/>");
                }
                if ((v & 4) > 0) {
                    text.append("<img src='" + iconsUrls.get(2) + "'/>");
                    hasTooltip = true;
                    tooltip.append("Zustimmung gesenkt von " + report.getAcceptanceBefore() + " auf " + report.getAcceptanceAfter());
                }
            }

            text.append("</html>");
            tooltip.append("</html>");

            l.setText(text.toString());
            if (hasTooltip) {
                l.setToolTipText(tooltip.toString());
            }
        } catch (Exception e) {
            //cast problem
            //  e.printStackTrace();
        }
        return l;
    }
}
