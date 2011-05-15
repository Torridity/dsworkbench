/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.models.SupportTroopsTableModel;
import de.tor.tribes.util.ImageUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import org.jdesktop.swingx.JXTreeTable;

/**
 *
 * @author Jejkal
 */
public class SupportTroopTableHeaderRenderer extends DefaultTableCellRenderer implements UIResource {

    private boolean horizontalTextPositionSet;

    public SupportTroopTableHeaderRenderer() {
        setHorizontalAlignment(JLabel.CENTER);
    }

    public void setHorizontalTextPosition(int textPosition) {
        horizontalTextPositionSet = true;
        super.setHorizontalTextPosition(textPosition);
    }

    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                Color fgColor = null;
                Color bgColor = null;
                if (hasFocus) {
                    fgColor = UIManager.getColor("TableHeader.focusCellForeground");
                    bgColor = UIManager.getColor("TableHeader.focusCellBackground");
                }
                if (fgColor == null) {
                    fgColor = header.getForeground();
                }
                if (bgColor == null) {
                    bgColor = header.getBackground();
                }
                setForeground(fgColor);
                setFont(header.getFont());
            }
        }

        SupportTroopsTableModel model = (SupportTroopsTableModel) ((JXTreeTable) table).getTreeTableModel();

        ImageIcon icon = model.getColumnIcon((String) value);
        BufferedImage i = ImageUtils.createCompatibleBufferedImage(18, 18, BufferedImage.BITMASK);
        Graphics2D g2d = i.createGraphics();
        if (icon != null) {
            icon.paintIcon(this, g2d, 0, 0);
            setText("");
            setIcon(new ImageIcon(i));
        } else {
            setIcon(null);
            setText(value == null ? "" : value.toString());
        }

        Border border = null;
        if (hasFocus) {
            border = UIManager.getBorder("TableHeader.focusCellBorder");
        }
        if (border == null) {
            border = UIManager.getBorder("TableHeader.cellBorder");
        }
        setBorder(border);

        return this;
    }
}
