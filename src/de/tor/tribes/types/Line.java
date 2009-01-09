/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.ui.MapPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.net.URLDecoder;
import org.jdom.Element;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *
 * @author Charon
 */
public class Line extends AbstractForm {

    private double xPosEnd = -1;
    private double yPosEnd = -1;
    private float strokeWidth = 1.0f;
    private boolean drawName = true;
    private Color drawColor = Color.WHITE;
    private float drawAlpha = 1.0f;
    private boolean startArrow = false;
    private boolean endArrow = false;

    public static Line fromXml(Element e) {
        try {
            Line l = new Line();
            Element elem = e.getChild("name");
            l.setFormName(URLDecoder.decode(elem.getTextTrim(), "UTF-8"));
            elem = e.getChild("pos");
            l.setXPos(Double.parseDouble(elem.getAttributeValue("x")));
            l.setYPos(Double.parseDouble(elem.getAttributeValue("y")));
            elem = e.getChild("textColor");
            l.setTextColor(new Color(Integer.parseInt(elem.getAttributeValue("r")), Integer.parseInt(elem.getAttributeValue("g")), Integer.parseInt(elem.getAttributeValue("b"))));
            l.setTextAlpha(Float.parseFloat(elem.getAttributeValue("a")));
            elem = e.getChild("drawColor");
            l.setDrawColor(new Color(Integer.parseInt(elem.getAttributeValue("r")), Integer.parseInt(elem.getAttributeValue("g")), Integer.parseInt(elem.getAttributeValue("b"))));
            l.setDrawAlpha(Float.parseFloat(elem.getAttributeValue("a")));
            elem = e.getChild("stroke");
            l.setStrokeWidth(Float.parseFloat(elem.getAttributeValue("width")));
            elem = e.getChild("arrow");
            l.setStartArrow(Boolean.parseBoolean(elem.getAttributeValue("start")));
            l.setEndArrow(Boolean.parseBoolean(elem.getAttributeValue("end")));
            elem = e.getChild("end");
            l.setXPosEnd(Double.parseDouble(elem.getAttributeValue("x")));
            l.setYPosEnd(Double.parseDouble(elem.getAttributeValue("y")));
            elem = e.getChild("textSize");
            l.setTextSize(Integer.parseInt(elem.getTextTrim()));
            return l;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void renderForm(Graphics2D g2d) {
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point2D.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPosEnd(), getYPosEnd());
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getCorrectedBounds();
        if (mapBounds.contains(s) || mapBounds.contains(e)) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            return;
        }

        //store properties
        Stroke before = g2d.getStroke();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        Color cBefore = g2d.getColor();
        g2d.setColor(getDrawColor());
        //start draw
        g2d.setFont(fBefore.deriveFont(getTextSize()));
        g2d.setStroke(getStroke());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getDrawAlpha()));
        g2d.drawLine((int) Math.rint(s.getX()), (int) Math.rint(s.getY()), (int) Math.rint(e.getX()), (int) Math.rint(e.getY()));
        drawDecoration(s, e, g2d);
        //reset properties
        g2d.setStroke(before);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);

        g2d.setColor(cBefore);
    }

    public void renderPreview(Graphics2D g2d) {
        Point2D.Double s = new Point2D.Double(getXPos(), getYPos());
        Point2D.Double e = new Point2D.Double(getXPosEnd(), getYPosEnd());
        //store properties
        Stroke before = g2d.getStroke();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        AffineTransform tb = g2d.getTransform();
        //start draw
        g2d.setFont(fBefore.deriveFont(getTextSize()));
        g2d.setStroke(getStroke());
        g2d.setColor(getDrawColor());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getDrawAlpha()));
        g2d.drawLine((int) Math.rint(s.getX()), (int) Math.rint(s.getY()), (int) Math.rint(e.getX()), (int) Math.rint(e.getY()));
        double theta = Math.atan2(e.y - s.y, e.x - s.x);
        double zoom = 1.0;
        double h = Math.sqrt(3) / 2 * 20 / zoom;

        if (isStartArrow()) {
            Point2D.Double p1 = new Point2D.Double(s.x, s.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(s.x, s.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(s.x - h, s.y);
            /* AffineTransform t = AffineTransform.getRotateInstance(theta, s.getX(), s.getY());
            g2d.setTransform(t);*/
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }
        if (isEndArrow()) {
            Point2D.Double p1 = new Point2D.Double(e.x, e.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(e.x, e.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(e.x + h, e.y);
            /*AffineTransform t = AffineTransform.getRotateInstance(theta, e.getX(), e.getY());
            g2d.setTransform(t);*/
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }

        if (isDrawName()) {
            /* Point2D.Double center = new Point2D.Double((e.getX() - s.getX()) / 2, (e.getY() - s.getY()) / 2);
            AffineTransform t = AffineTransform.getRotateInstance(theta, s.x + center.getX(), s.y + center.getY());
            g2d.setTransform(t);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
            g2d.drawString(getFormName(), (int) Math.rint(s.x + center.getX()), (int) Math.rint(s.y + center.getY()));
             */

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setColor(getTextColor());
            g2d.drawString(getFormName(), (int) s.x, (int) s.y);
        }
        //reset properties
        g2d.setStroke(before);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
        g2d.setTransform(tb);
    }

    private void drawDecoration(Point2D.Double s, Point2D.Double e, Graphics2D g2d) {
        double theta = Math.atan2(e.y - s.y, e.x - s.x);
        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        double h = Math.sqrt(3) / 2 * 20 / zoom;
        AffineTransform tb = g2d.getTransform();

        if (isStartArrow()) {
            Point2D.Double p1 = new Point2D.Double(s.x, s.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(s.x, s.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(s.x - h, s.y);
            AffineTransform t = AffineTransform.getRotateInstance(theta, s.getX(), s.getY());
            g2d.setTransform(t);
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }
        if (isEndArrow()) {
            Point2D.Double p1 = new Point2D.Double(e.x, e.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(e.x, e.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(e.x + h, e.y);
            AffineTransform t = AffineTransform.getRotateInstance(theta, e.getX(), e.getY());
            g2d.setTransform(t);
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }
        g2d.setTransform(tb);
        if (isDrawName()) {
            /* Point2D.Double center = new Point2D.Double((e.getX() - s.getX()) / 2, (e.getY() - s.getY()) / 2);
            AffineTransform t = AffineTransform.getRotateInstance(theta, s.x + center.getX(), s.y + center.getY());
            g2d.setTransform(t);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
            g2d.drawString(getFormName(), (int) Math.rint(s.x + center.getX()), (int) Math.rint(s.y + center.getY()));
             */
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setColor(getTextColor());
            g2d.drawString(getFormName(), (int) s.x, (int) s.y);
        }
    }

    @Override
    protected String getFormXml() {
        String xml = "<end x=\"" + getXPosEnd() + "\" y=\"" + getYPosEnd() + "\"/>\n";
        xml += "<drawColor r=\"" + getDrawColor().getRed() + "\" g=\"" + getDrawColor().getGreen() + "\" b=\"" + getDrawColor().getBlue() + "\" a=\"" + getDrawAlpha() + "\"/>\n";
        xml += "<stroke width=\"" + getStrokeWidth() + "\"/>\n";
        xml += "<arrow start=\"" + isStartArrow() + "\" end=\"" + isEndArrow() + "\"/>\n";
        xml += "<drawName>" + isDrawName() + "</drawName>\n";
        return xml;
    }

    @Override
    public String getFormType() {
        return "line";
    }

    /**
     * @return the color
     */
    public Color getDrawColor() {
        return drawColor;
    }

    /**
     * @param color the color to set
     */
    public void setDrawColor(Color color) {
        this.drawColor = color;
    }

    /**
     * @return the strokeWidth
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * @param strokeWidth the strokeWidth to set
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * @return the stroke
     */
    public BasicStroke getStroke() {
        float w = (float) (getStrokeWidth() / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
        return new BasicStroke(w);
    }

    @Override
    public void setXPos(double xPos) {
        super.setXPos(xPos);
        if (xPosEnd == -1) {
            setXPosEnd(xPos);
        }
    }

    @Override
    public void setYPos(double yPos) {
        super.setYPos(yPos);
        if (xPosEnd == -1) {
            setXPosEnd(yPos);
        }
    }

    /**
     * @return the xPosEnd
     */
    public double getXPosEnd() {
        return xPosEnd;
    }

    /**
     * @param xPosEnd the xPosEnd to set
     */
    public void setXPosEnd(double xPosEnd) {
        this.xPosEnd = xPosEnd;
    }

    /**
     * @return the yPosEnd
     */
    public double getYPosEnd() {
        return yPosEnd;
    }

    /**
     * @param yPosEnd the yPosEnd to set
     */
    public void setYPosEnd(double yPosEnd) {
        this.yPosEnd = yPosEnd;
    }

    /**
     * @return the drawName
     */
    public boolean isDrawName() {
        return drawName;
    }

    /**
     * @param drawName the drawName to set
     */
    public void setDrawName(boolean drawName) {
        this.drawName = drawName;
    }

    /**
     * @return the drawAlpha
     */
    public float getDrawAlpha() {
        return drawAlpha;
    }

    /**
     * @param drawAlpha the drawAlpha to set
     */
    public void setDrawAlpha(float drawAlpha) {
        this.drawAlpha = drawAlpha;
    }

    /**
     * @return the startArrow
     */
    public boolean isStartArrow() {
        return startArrow;
    }

    /**
     * @param startArrow the startArrow to set
     */
    public void setStartArrow(boolean startArrow) {
        this.startArrow = startArrow;
    }

    /**
     * @return the endArrow
     */
    public boolean isEndArrow() {
        return endArrow;
    }

    /**
     * @param endArrow the endArrow to set
     */
    public void setEndArrow(boolean endArrow) {
        this.endArrow = endArrow;
    }
}
