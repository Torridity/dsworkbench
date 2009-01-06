/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.PatchFontMetrics;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class Text extends AbstractForm {

    public static AbstractForm fromXml(Element e) {
        try {
            Text l = new Text();
            Element elem = e.getChild("name");
            l.setFormName(URLDecoder.decode(elem.getTextTrim(), "UTF-8"));
            elem = e.getChild("pos");
            l.setXPos(Double.parseDouble(elem.getAttributeValue("x")));
            l.setYPos(Double.parseDouble(elem.getAttributeValue("y")));
            elem = e.getChild("textColor");
            l.setTextColor(new Color(Integer.parseInt(elem.getAttributeValue("r")), Integer.parseInt(elem.getAttributeValue("g")), Integer.parseInt(elem.getAttributeValue("b"))));
            l.setTextAlpha(Float.parseFloat(elem.getAttributeValue("a")));
            elem = e.getChild("textSize");
            l.setTextSize(Float.parseFloat(elem.getTextTrim()));
            return l;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void renderForm(Graphics2D g2d) {
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getBounds();
        FontMetrics met = PatchFontMetrics.patch(g2d.getFontMetrics());
        Rectangle2D bounds = met.getStringBounds(getFormName(), g2d);
        if (mapBounds.intersects(bounds)) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            return;
        }
        //save properties
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setFont(fBefore.deriveFont(getTextSize()));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
        g2d.setColor(getTextColor());
        Point s = MapPanel.getSingleton().virtualPosToSceenPos(getXPos(), getYPos());
        g2d.drawString(getFormName(), s.x, s.y);
        //restore properties
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    public void renderPreview(Graphics2D g2d) {
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getBounds();
        FontMetrics met = PatchFontMetrics.patch(g2d.getFontMetrics());
        Rectangle2D bounds = met.getStringBounds(getFormName(), g2d);
        if (mapBounds.intersects(bounds)) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            return;
        }
        //save properties
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setFont(fBefore.deriveFont(getTextSize()));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
        g2d.setColor(getTextColor());
        Point2D.Double s = new Point2D.Double(getXPos(), getYPos());
        g2d.drawString(getFormName(), (int) s.x, (int) s.y);
        //restore properties
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    @Override
    protected String getFormXml() {
        return "";
    }

    @Override
    public String getFormType() {
        return "text";
    }
}
