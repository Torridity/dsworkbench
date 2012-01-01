/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Jejkal
 */
public class AttackMiscInfoRenderer extends DefaultTableRenderer {

    private List<String> iconsUrls = null;

    public AttackMiscInfoRenderer() {
        super();
        try {
            iconsUrls = new LinkedList<String>();
            iconsUrls.add(AttackMiscInfoRenderer.class.getResource("/res/ui/att_browser.png").toURI().toString());
            iconsUrls.add(AttackMiscInfoRenderer.class.getResource("/res/ui/att_browser_unsent.png").toURI().toString());
            iconsUrls.add(AttackMiscInfoRenderer.class.getResource("/res/ui/pencil2.png").toURI().toString());
            iconsUrls.add(AttackMiscInfoRenderer.class.getResource("/res/ui/not_drawn.png").toURI().toString());
        } catch (Exception e) {
            iconsUrls = null;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        
        try {
           /* label.setHorizontalAlignment(SwingConstants.CENTER);
            Attack a = (Attack) value;
            StringBuilder text = new StringBuilder();
            text.append("<html>");
            boolean shown = a.isShowOnMap();
            boolean transfer = a.isTransferredToBrowser();
            if (shown) {
                text.append("<img src='").append(iconsUrls.get(2)).append("' width='16' height='16'/>");//
            } else {
                text.append("<img src='").append(iconsUrls.get(3)).append("' width='16' height='16'/>");
            }

            text.append("&nbsp;&nbsp;");
            if (transfer) {
                text.append("<img src='").append(iconsUrls.get(0)).append("' style='padding-left:10px;' width='16' height='16'/>");
            } else {
                text.append("<img src='").append(iconsUrls.get(1)).append("' style='padding-left:10px;' width='16' height='16'/>");
            }
            text.append("</html>");
            label.setText(text.toString());
            label.setToolTipText("<html>Der Angriff " + ((!shown) ? "<b>ist nicht</b>" : "<b>ist</b>") + " auf der Karte eingezeichnet<br/>"
                    + "Der Angriff " + ((!transfer) ? "<b>wurde noch nicht</b>" : "<b>wurde bereits</b>") + " in den Browser &uuml;bertragen</html>");
      */ } catch (Exception e) {
            //cast problem
        }
        return label;
    }
}
