/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ServerSettings;
import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 * @author Jejkal
 */
public class ColoredDateCellRenderer extends DefaultTableRenderer {
    
    private SimpleDateFormat specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
    private final int MINUTE = (1000 * 60);
    
    public ColoredDateCellRenderer() {
        super();
        if (!ServerSettings.getSingleton().isMillisArrival()) {
            specialFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        }
    }
    
    public ColoredDateCellRenderer(String pPattern) {
        this();
        specialFormat = new SimpleDateFormat(pPattern);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel renderComponent = ((JLabel) c);
        Date d = (Date) value;
        long t = 0;
        if (d == null) {
            d = new Date();
        }
        long now = System.currentTimeMillis();
        t = d.getTime();
        renderComponent.setText(specialFormat.format(d));
        
        long diff = t - now;
        long five_minutes = 5 * MINUTE;
        long ten_minutes = 10 * MINUTE;
        Color color = null;
        
        if (row % 2 == 0) {
            color = Constants.DS_ROW_A;
        } else {
            color = Constants.DS_ROW_B;
        }
        
        if (t <= now) {
            //value is expired, stroke result
            renderComponent.setText(specialFormat.format(d));
            renderComponent.setForeground(Color.RED);
        } else if (diff <= ten_minutes && diff > five_minutes) {
            float ratio = (float) (diff - five_minutes) / (float) five_minutes;
            Color c1 = Color.YELLOW;
            Color c2 = Color.GREEN;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            color = new Color(red, green, blue);
        } else if (diff <= five_minutes) {
            float ratio = (float) diff / (float) five_minutes;
            Color c1 = Color.RED;
            Color c2 = Color.YELLOW;
            int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
            int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
            int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
            color = new Color(red, green, blue);
        } else {
            //default renderer and color
            renderComponent.setText(specialFormat.format((Date) value));
        }
        
        
        if (isSelected) {
            color = c.getBackground();
        }
        renderComponent.setOpaque(true);
        renderComponent.setBackground(color);
        return renderComponent;
    }
}
