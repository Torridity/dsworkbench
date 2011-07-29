/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.ui.components.ColoredProgressBar;
import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author Charon
 */
public class PercentCellRenderer extends DefaultTableRenderer {

    private NumberFormat format = NumberFormat.getInstance();

    public PercentCellRenderer() {
        super();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
    }

    public PercentCellRenderer(NumberFormat pCustomFormat) {
        this();
        format = pCustomFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        

        ColoredProgressBar p = new ColoredProgressBar(0, 20);

        Float val = (Float) value;

        if (val <= 0.1f) {
            p.setForeground(Color.RED);
        } else if (val <= 0.5f) {
            p.setForeground(Color.YELLOW);
        } else {
            p.setForeground(Color.GREEN);
        }

      //  p.setString(Integer.toString(val));
        p.setStringPainted(true);
        p.setValue(Math.round(val));

        return p;
    }
    /* @Override
    public void paint(Graphics g) {
    //super.paint(g);
    Graphics2D g2d = (Graphics2D) g;
    String t = getText();
    try {
    g2d.setColor(Constants.DS_ROW_B);
    g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
    float v = Float.parseFloat(t);
    if (v <= 0.1f) {
    g2d.setColor(Color.RED);
    } else if (v <= 0.5f) {
    g2d.setColor(Color.YELLOW);   
    } else {
    g2d.setColor(Color.GREEN);
    }
    t = format.format(v * 100) + "%";
    int w = Math.round(getWidth() * v);
    g2d.fill3DRect(0, 0, w, getHeight(), true);
    g2d.setColor(Color.BLACK);
    Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(t, g);
    double x = (double) getWidth() / 2.0 - bounds.getWidth() / 2.0;
    double y = (double) getHeight() / 2.0 - bounds.getHeight() / 2.0;
    g2d.drawString(t, (int) Math.round(x - bounds.getX()), (int) Math.round(y - bounds.getY()));
    g2d.dispose();
    } catch (Exception e) {
    super.paint(g);
    }
    
    }
    
    @Override
    public void setValue(Object value) {
    try {
    setText(value.toString());
    } catch (Exception e) {
    if (value != null) {
    setText(value.toString());
    } else {
    setText("0.0");
    }
    }
    }*/
}
