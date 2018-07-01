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
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import org.jdom2.Element;

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

    @Override
    public void formFromXml(Element e) {
        try {
            Element elem = e.getChild("drawColor");
            this.drawColor = new Color(Integer.parseInt(elem.getAttributeValue("r")), Integer.parseInt(elem.getAttributeValue("g")), Integer.parseInt(elem.getAttributeValue("b")));
            this.drawAlpha = Float.parseFloat(elem.getAttributeValue("a"));
            elem = e.getChild("stroke");
            this.strokeWidth = Float.parseFloat(elem.getAttributeValue("width"));
            elem = e.getChild("arrow");
            this.startArrow = Boolean.parseBoolean(elem.getAttributeValue("start"));
            this.endArrow = Boolean.parseBoolean(elem.getAttributeValue("end"));
            elem = e.getChild("end");
            this.xPosEnd = Double.parseDouble(elem.getAttributeValue("x"));
            this.yPosEnd = Double.parseDouble(elem.getAttributeValue("y"));
            elem = e.getChild("drawName");
            this.drawName = Boolean.parseBoolean(elem.getTextTrim());
        } catch (Exception ignored) {
        }
    }

    @Override
    protected Element formToXml(String elementName) {
        Element line = new Element(elementName);
        try {
            line.addContent(new Element("stroke").setAttribute("width", Float.toString(strokeWidth)));
            line.addContent(new Element("drawName").setText(Boolean.toString(drawName)));
            
            Element elm = new Element("end");
            elm.setAttribute("x", Double.toString(xPosEnd));
            elm.setAttribute("y", Double.toString(yPosEnd));
            line.addContent(elm);
            
            elm = new Element("drawColor");
            elm.setAttribute("r", Integer.toString(drawColor.getRed()));
            elm.setAttribute("g", Integer.toString(drawColor.getGreen()));
            elm.setAttribute("b", Integer.toString(drawColor.getBlue()));
            elm.setAttribute("a", Float.toString(drawAlpha));
            line.addContent(elm);
            
            elm = new Element("arrow");
            elm.setAttribute("start", Boolean.toString(startArrow));
            elm.setAttribute("end", Boolean.toString(endArrow));
            line.addContent(elm);
        } catch (Exception ignored) {
        }
        return line;
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
        String endXVal = Integer.toString((int) Math.rint(xPosEnd));
        String endYVal = Integer.toString((int) Math.rint(yPosEnd));
        String widthVal = Integer.toString((int) Math.rint(xPosEnd - getXPos()));
        String heightVal = Integer.toString((int) Math.rint(yPosEnd - getYPos()));
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
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point2D.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(xPosEnd, yPosEnd);
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getBounds();

        setVisibleOnMap(mapBounds.intersectsLine(new Line2D.Double(s, e)));
        if (!isVisibleOnMap()) {
            return;
        }

        //store properties
        Stroke before = g2d.getStroke();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        Color cBefore = g2d.getColor();
        checkShowMode(g2d, drawColor);
        //start draw
        g2d.setFont(fBefore.deriveFont((float) getTextSize()));
        g2d.setStroke(getStroke());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawAlpha));
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
        Point2D.Double e = new Point2D.Double(xPosEnd, yPosEnd);
        //store properties
        Stroke before = g2d.getStroke();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        AffineTransform tb = g2d.getTransform();
        //start draw
        g2d.setFont(fBefore.deriveFont((float) getTextSize()));
        g2d.setStroke(getStroke());
        g2d.setColor(drawColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawAlpha));
        g2d.drawLine((int) Math.rint(s.getX()), (int) Math.rint(s.getY()), (int) Math.rint(e.getX()), (int) Math.rint(e.getY()));
        double zoom = 1.0;
        double h = Math.sqrt(3) / 2 * 20 / zoom;

        if (startArrow) {
            Point2D.Double p1 = new Point2D.Double(s.x, s.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(s.x, s.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(s.x - h, s.y);
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }
        if (endArrow) {
            Point2D.Double p1 = new Point2D.Double(e.x, e.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(e.x, e.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(e.x + h, e.y);
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }

        if (drawName) {
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

    @Override
    public List<Village> getContainedVillages() {
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point2D.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(xPosEnd, yPosEnd);

        List<Village> result = MapPanel.getSingleton().getVillagesOnLine(new Line2D.Double((int) Math.rint(s.getX()), (int) Math.rint(s.getY()), (int) Math.rint(e.getX()), (int) Math.rint(e.getY())));
        if (result == null) {
            return super.getContainedVillages();
        }
        return result;
    }

    @Override
    public java.awt.Rectangle getBounds() {
        Point2D.Double s = new Point2D.Double(getXPos(), getYPos());
        Point2D.Double e = new Point2D.Double(xPosEnd, yPosEnd);
        int x = (int) Math.round((s.x < e.x) ? s.x : e.x);
        int y = (int) Math.round((s.y < e.y) ? s.y : e.y);
        int w = (int) Math.round(Math.abs(s.x - e.x));
        int h = (int) Math.round(Math.abs(s.y - e.y));
        return new java.awt.Rectangle(x, y, w, h);
    }

    private void drawDecoration(Point2D.Double s, Point2D.Double e, Graphics2D g2d) {
        double theta = Math.atan2(e.y - s.y, e.x - s.x);

        double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        double h = Math.sqrt(3) / 2 * 20 / zoom;
        AffineTransform tb = g2d.getTransform();

        if (startArrow) {
            Point2D.Double p1 = new Point2D.Double(s.x, s.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(s.x, s.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(s.x - h, s.y);
            AffineTransform t = AffineTransform.getRotateInstance(theta, s.getX(), s.getY());
            g2d.setTransform(t);
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }
        if (endArrow) {
            Point2D.Double p1 = new Point2D.Double(e.x, e.y - h / 2);
            Point2D.Double p2 = new Point2D.Double(e.x, e.y + h / 2);
            Point2D.Double p3 = new Point2D.Double(e.x + h, e.y);
            AffineTransform t = AffineTransform.getRotateInstance(theta, e.getX(), e.getY());
            g2d.setTransform(t);
            Polygon poly = new Polygon(new int[]{(int) Math.rint(p1.x), (int) Math.rint(p2.x), (int) Math.rint(p3.x)}, new int[]{(int) Math.rint(p1.y), (int) Math.rint(p2.y), (int) Math.rint(p3.y)}, 3);
            g2d.fillPolygon(poly);
        }
        g2d.setTransform(tb);
        if (drawName) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setColor(getTextColor());
            g2d.drawString(getFormName(), (int) s.x, (int) s.y);
        }
    }

    @Override
    public FORM_TYPE getFormType() {
        return FORM_TYPE.LINE;
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
        float w = (float) (strokeWidth / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
        return new BasicStroke(w);
    }

    @Override
    public void setXPos(double xPos) {
        super.setXPos(xPos);
        if (xPosEnd == -1) {
            this.xPosEnd = xPos;
        }
    }

    @Override
    public void setYPos(double yPos) {
        super.setYPos(yPos);
        if (yPosEnd == -1) {
            this.yPosEnd = yPos;
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
