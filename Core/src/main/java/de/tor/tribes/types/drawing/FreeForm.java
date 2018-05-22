/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.types.drawing;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.bb.VillageListFormatter;
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
 * @author Torridity
 */
public class FreeForm extends AbstractForm {

    private List<Point2D.Double> points = null;
    private boolean filled = false;
    private float strokeWidth = 1.0f;
    private int rounding = 0;
    private boolean drawName = true;
    private Color drawColor = Color.WHITE;
    private float drawAlpha = 1.0f;
    private float toler = 0.5f;
    private boolean closed = false;

    @Override
    public void loadFromXml(Element e) {
        try {
            Element elem = e.getChild("name");
            setFormName(URLDecoder.decode(elem.getTextTrim(), "UTF-8"));
            elem = e.getChild("pos");
            setXPos(Double.parseDouble(elem.getAttributeValue("x")));
            setYPos(Double.parseDouble(elem.getAttributeValue("y")));
            elem = e.getChild("textColor");
            setTextColor(new Color(Integer.parseInt(elem.getAttributeValue("r")), Integer.parseInt(elem.getAttributeValue("g")), Integer.parseInt(elem.getAttributeValue("b"))));
            setTextAlpha(Float.parseFloat(elem.getAttributeValue("a")));
            elem = e.getChild("drawColor");
            this.drawColor = new Color(Integer.parseInt(elem.getAttributeValue("r")), Integer.parseInt(elem.getAttributeValue("g")), Integer.parseInt(elem.getAttributeValue("b")));
            this.drawAlpha = Float.parseFloat(elem.getAttributeValue("a"));
            elem = e.getChild("stroke");
            this.strokeWidth = Float.parseFloat(elem.getAttributeValue("width"));
            elem = e.getChild("filled");
            this.filled = Boolean.parseBoolean(elem.getTextTrim());
            elem = e.getChild("textSize");
            setTextSize(Integer.parseInt(elem.getTextTrim()));
            elem = e.getChild("points");
            List<Element> pChildren = elem.getChildren("point");
            for (Element child : pChildren) {
                double x = Double.parseDouble(child.getAttribute("x").getValue());
                double y = Double.parseDouble(child.getAttribute("y").getValue());
                addPointWithoutCheck(new Point2D.Double(x, y));
            }
            elem = e.getChild("drawName");
            this.drawName = Boolean.parseBoolean(elem.getTextTrim());
        } catch (Exception ignored) {
        }
    }

    public FreeForm() {
        super();
        points = new LinkedList<>();
    }

    @Override
    public boolean allowsBBExport() {
        return true;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        String nameVal = getFormName();
        if (nameVal == null || nameVal.length() == 0) {
            nameVal = "Kein Name";
        }
        String startXVal = Integer.toString((int) Math.rint(getXPos()));
        String startYVal = Integer.toString((int) Math.rint(getYPos()));
        String endXVal = Integer.toString((int) Math.rint(getBounds().getX() + getBounds().getWidth()));
        String endYVal = Integer.toString((int) Math.rint(getBounds().getY() + getBounds().getHeight()));
        String widthVal = Integer.toString((int) Math.rint(getBounds().getWidth()));
        String heightVal = Integer.toString((int) Math.rint(getBounds().getHeight()));
        String colorVal = "";
        if (drawColor != null) {
            colorVal = "#" + Integer.toHexString(drawColor.getRGB() & 0x00ffffff);
        } else {
            colorVal = "#" + Integer.toHexString(Color.BLACK.getRGB() & 0x00ffffff);
        }
        List<Village> containedVillages = getContainedVillages();
        String villageListVal = "";
        if (containedVillages != null && !containedVillages.isEmpty()) {
            villageListVal = new VillageListFormatter().formatElements(containedVillages, pExtended);
        } else {
            villageListVal = "Keine Dörfer enthalten";
        }
        return new String[]{nameVal, startXVal, startYVal, widthVal, heightVal, endXVal, endYVal, colorVal, villageListVal};
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
        checkShowMode(g2d, drawColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawAlpha));

        Point2D.Double pp = MapPanel.getSingleton().virtualPosToSceenPosDouble(points.get(0).getX(), points.get(0).getY());
        GeneralPath p = new GeneralPath();
        p.moveTo(pp.x, pp.y);
        for (int i = 1; i < points.size(); i++) {
            pp = MapPanel.getSingleton().virtualPosToSceenPosDouble(points.get(i).getX(), points.get(i).getY());
            p.lineTo(pp.x, pp.y);
        }
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getBounds();
        if (mapBounds.intersects(p.getBounds())) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            return;
        }
        if (filled) {
            g2d.fill(p);
        } else {
            g2d.draw(p);
        }

        if (drawName) {
            g2d.setColor(getTextColor());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setFont(fBefore.deriveFont((float) getTextSize()));
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

    @Override
    public List<Village> getContainedVillages() {
        Point2D.Double pp = MapPanel.getSingleton().virtualPosToSceenPosDouble(points.get(0).getX(), points.get(0).getY());
        GeneralPath p = new GeneralPath();
        p.moveTo(pp.x, pp.y);
        for (int i = 1; i < points.size(); i++) {
            pp = MapPanel.getSingleton().virtualPosToSceenPosDouble(points.get(i).getX(), points.get(i).getY());
            p.lineTo(pp.x, pp.y);
        }
        p.closePath();
        List<Village> result = MapPanel.getSingleton().getVillagesInShape(p);
        if (result == null) {
            return super.getContainedVillages();
        }
        return result;
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
        g2d.setColor(drawColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawAlpha));

        GeneralPath p = new GeneralPath();
        Point2D.Double pp = points.get(0);
        p.moveTo(pp.x, pp.y);
        for (int i = 0; i <= points.size() - 1; i++) {
            pp = points.get(i);
            p.lineTo(pp.x, pp.y);
        }

        if (filled) {
            g2d.fill(p);
        } else {
            g2d.draw(p);
        }

        if (drawName) {
            g2d.setColor(getTextColor());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setFont(fBefore.deriveFont((float) getTextSize()));
            Rectangle2D textBounds = g2d.getFontMetrics().getStringBounds(getFormName(), g2d);
            java.awt.Rectangle bounds = p.getBounds();
            g2d.drawString(getFormName(), (int) Math.rint(bounds.getX() + bounds.getWidth() / 2 - textBounds.getWidth() / 2), (int) Math.rint(bounds.getY() + bounds.getHeight() / 2 + textBounds.getHeight() / 2));
        }
        g2d.setStroke(sBefore);
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    @Override
    public java.awt.Rectangle getBounds() {
        GeneralPath p = new GeneralPath();
        p.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            p.lineTo(points.get(i).x, points.get(i).y);
        }

        Rectangle2D r2d = p.getBounds2D();
        return new java.awt.Rectangle((int) r2d.getX(), (int) r2d.getY(), (int) r2d.getWidth(), (int) r2d.getHeight());
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
        boolean line = lineContainsPoint(p2.x, p2.y, p1.x, p1.y, p3.x, p3.y, (double) toler);
        if (line) {
            points.set(points.size() - 1, pNewPoint);
        } else {
            if (!((last.getX() == pNewPoint.getX()) && (last.getY() == pNewPoint.getY()))) {
                points.add(pNewPoint);
            }
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
        StringBuilder b = new StringBuilder();
        b.append("<drawColor r=\"").append(drawColor.getRed()).append("\" g=\"").append(drawColor.getGreen()).append("\" b=\"").append(drawColor.getBlue()).append("\" a=\"").append(drawAlpha).append( "\"/>\n");
        b.append("<filled>").append(filled).append("</filled>\n");
        b.append("<stroke width=\"").append(strokeWidth).append( "\"/>\n");
        b.append("<drawName>").append(drawName).append( "</drawName>\n");
        b.append("<tolerance>").append(toler).append( "</tolerance>\n");
        b.append( "<points>\n");
        for (Point2D.Double p : points) {
            b.append("<point x=\"").append(p.getX()).append("\" y=\"").append(p.getY()).append("\"/>\n");
        }
        b.append( "</points>\n");
        return b.toString();
    }

    @Override
    public FORM_TYPE getFormType() {
        return FORM_TYPE.FREEFORM;
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
        if (points.isEmpty()) {
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
        if (points.isEmpty()) {
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
        float w = (float) (strokeWidth / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
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
    public float getTolerance() {
        return toler;
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setTolerance(float tolerance) {
        toler = tolerance;
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
    public void setBClosed(boolean bClosed) {
        this.closed = bClosed;
    }
}
