/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.MapPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author Jejkal
 */
public class FreeForm extends AbstractForm {

    private List<Point2D.Double> points = null;
    private boolean filled = false;
    private float strokeWidth = 1.0f;
    private int rounding = 0;
    private boolean drawName = true;
    private Color drawColor = Color.WHITE;
    private float drawAlpha = 1.0f;
    private double tolerance = 0.5;
    private boolean closed = false;

    public static AbstractForm fromXml(Element e) {
        try {
            FreeForm l = new FreeForm();
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
            elem = e.getChild("filled");
            l.setFilled(Boolean.parseBoolean(elem.getTextTrim()));
            elem = e.getChild("textSize");
            l.setTextSize(Float.parseFloat(elem.getTextTrim()));
            elem = e.getChild("points");
            List<Element> pChildren = elem.getChildren("point");
            for (Element child : pChildren) {
                double x = Double.parseDouble(child.getAttribute("x").getValue());
                double y = Double.parseDouble(child.getAttribute("y").getValue());
                l.addPointWithoutCheck(new Point2D.Double(x, y));
            }
            return l;
        } catch (Exception ex) {
            return null;
        }
    }

    public FreeForm() {
        super();
        points = new LinkedList<Point2D.Double>();
    }

    @Override
    public void renderForm(Graphics2D g2d) {
        if (points.size() < 1) {
            return;
        }
        //store properties
        Stroke sBefore = g2d.getStroke();
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setStroke(getStroke());
        g2d.setColor(getDrawColor());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getDrawAlpha()));
        GeneralPath p = new GeneralPath();
        Point2D.Double pp = MapPanel.getSingleton().virtualPosToSceenPosDouble(points.get(0).getX(), points.get(0).getY());
        p.moveTo(pp.x, pp.y);
        for (int i = 0; i <= points.size() - 1; i++) {
            pp = MapPanel.getSingleton().virtualPosToSceenPosDouble(points.get(i).getX(), points.get(i).getY());
            p.lineTo(pp.x, pp.y);
        }

        if (isFilled()) {
            g2d.fill(p);
        } else {
            g2d.draw(p);
        }
        if (isDrawName()) {
            g2d.setColor(getTextColor());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setFont(fBefore.deriveFont(getTextSize()));
            Rectangle2D textBounds = g2d.getFontMetrics().getStringBounds(getFormName(), g2d);
            java.awt.Rectangle bounds = p.getBounds();
            g2d.drawString(getFormName(), (int) Math.rint(bounds.getX() + bounds.getWidth() / 2 - textBounds.getWidth() / 2), (int) Math.rint(bounds.getY() + bounds.getHeight() / 2 + textBounds.getHeight() / 2));
        }
        //restore properties
        g2d.setStroke(sBefore);
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    public void renderPreview(Graphics2D g2d) {
        if (points.size() < 1) {
            return;
        }
        //store properties
        Stroke sBefore = g2d.getStroke();
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setStroke(getStroke());
        g2d.setColor(getDrawColor());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getDrawAlpha()));

        GeneralPath p = new GeneralPath();
        Point2D.Double pp = points.get(0);
        p.moveTo(pp.x, pp.y);
        for (int i = 0; i <= points.size() - 1; i++) {
            pp = points.get(i);
            p.lineTo(pp.x, pp.y);
        }

        if (isFilled()) {
            g2d.fill(p);
        } else {
            g2d.draw(p);
        }
        if (isDrawName()) {
            g2d.setColor(getTextColor());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setFont(fBefore.deriveFont(getTextSize()));
            Rectangle2D textBounds = g2d.getFontMetrics().getStringBounds(getFormName(), g2d);
            java.awt.Rectangle bounds = p.getBounds();
            g2d.drawString(getFormName(), (int) Math.rint(bounds.getX() + bounds.getWidth() / 2 - textBounds.getWidth() / 2), (int) Math.rint(bounds.getY() + bounds.getHeight() / 2 + textBounds.getHeight() / 2));
        }
        g2d.setStroke(sBefore);
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    /**For reading from XML only*/
    private void addPointWithoutCheck(Point2D.Double pNewPoint) {
        points.add(pNewPoint);
    }

    public void addPoint(Point2D.Double pNewPoint) {
        if (points.size() < 2) {
            points.add(pNewPoint);
            return;
        }
        Point2D.Double secondLast = points.get(points.size() - 2);
        Point2D.Double last = points.get(points.size() - 1);
        Point p1 = MapPanel.getSingleton().virtualPosToSceenPos(secondLast.getX(), secondLast.getY());
        Point p2 = MapPanel.getSingleton().virtualPosToSceenPos(pNewPoint.getX(), pNewPoint.getY());
        Point p3 = MapPanel.getSingleton().virtualPosToSceenPos(last.getX(), last.getY());
        boolean line = lineContainsPoint(p2.x, p2.y, p1.x, p1.y, p3.x, p3.y, getTolerance());
        if (line) {
            points.set(points.size() - 1, pNewPoint);
        } else {
            points.add(pNewPoint);
        }
    }

    public boolean lineContainsPoint(int x1, int y1, int x2, int y2, int px, int py, double tolerance) {
        double a, b, x, y;

        if (x1 == x2) {
            return (Math.abs(px - x1) <= tolerance);
        }
        if (y1 == y2) {
            return (Math.abs(py - y1) <= tolerance);
        }

        a = (double) (y1 - y2) / (double) (x1 - x2);
        b = (double) y1 - a * (double) x1;
        x = (py - b) / a;
        y = a * px + b;

        return (Math.min(Math.abs(x - px), Math.abs(y - py)) <= tolerance);
    }

    @Override
    protected String getFormXml() {
        String result = "<tolerance>" + getTolerance() + "</tolerance>\n";
        result += "<points>\n";
        for (Point2D.Double p : points) {
            result += "<point x=\"" + p.getX() + "\" y=\"" + p.getY() + "\"/>\n";
        }
        result += "<points>\n";
        return result;
    }

    @Override
    public String getFormType() {
        return "freeform";
    }

    @Override
    public double getXPos() {
        Point2D.Double p0 = points.get(0);
        if (p0 == null) {
            return 0.0;
        }
        return p0.getX();
    }

    @Override
    public void setXPos(double xPos) {
        if (points.size() == 0) {
            points.add(0, new Point2D.Double(xPos, 0.0));
        } else {
            Point2D.Double p0 = points.get(0);
            points.set(0, new Point2D.Double(xPos, p0.getX()));
        }
    }

    @Override
    public double getYPos() {
        Point2D.Double p0 = points.get(0);
        if (p0 == null) {
            return 0.0;
        }
        return p0.getY();
    }

    @Override
    public void setYPos(double yPos) {
        if (points.size() == 0) {
            points.add(0, new Point2D.Double(0.0, yPos));
        } else {
            Point2D.Double p0 = points.get(0);
            points.set(0, new Point2D.Double(p0.getX(), yPos));
        }
    }

    /**
     * @return the filled
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * @param filled the filled to set
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    /**
     * @return the stroke
     */
    public BasicStroke getStroke() {
        float w = (float) (getStrokeWidth() / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
        return new BasicStroke(w);
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
     * @return the rounding
     */
    public int getRounding() {
        return rounding;
    }

    /**
     * @param rounding the rounding to set
     */
    public void setRounding(int rounding) {
        this.rounding = rounding;
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
     * @return the drawColor
     */
    public Color getDrawColor() {
        return drawColor;
    }

    /**
     * @param drawColor the drawColor to set
     */
    public void setDrawColor(Color drawColor) {
        this.drawColor = drawColor;
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
     * @return the points
     */
    public List<Point2D.Double> getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(List<Point2D.Double> points) {
        this.points = points;
    }

    /**
     * @return the tolerance
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * @return the bClosed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param bClosed the bClosed to set
     */
    public void setBClosed(boolean closed) {
        this.closed = closed;
    }
}
