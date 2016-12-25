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
import org.jdom.Element;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URLDecoder;
import java.util.List;

/**
 *
 * @author Charon
 */
public class Circle extends AbstractForm {

    private double xPosEnd = -1;
    private double yPosEnd = -1;
    private boolean filled = false;
    private float strokeWidth = 1.0f;
    private boolean drawName = false;
    private Color drawColor = Color.WHITE;
    private float drawAlpha = 1.0f;

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
            elem = e.getChild("end");
            this.xPosEnd = Double.parseDouble(elem.getAttributeValue("x"));
            this.yPosEnd = Double.parseDouble(elem.getAttributeValue("y"));
            elem = e.getChild("filled");
            this.filled = Boolean.parseBoolean(elem.getTextTrim());
            elem = e.getChild("textSize");
            setTextSize(Integer.parseInt(elem.getTextTrim()));
            elem = e.getChild("drawName");
            this.drawName = Boolean.parseBoolean(elem.getTextTrim());
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<Village> getContainedVillages() {
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(xPosEnd, yPosEnd);
        int x = (int) Math.rint((s.getX() < e.getX()) ? s.getX() : e.getX());
        int y = (int) Math.rint((s.getY() < e.getY()) ? s.getY() : e.getY());
        int w = (int) Math.rint(Math.abs(s.getX() - e.getX()));
        int h = (int) Math.rint(Math.abs(s.getY() - e.getY()));

        List<Village> result = MapPanel.getSingleton().getVillagesInShape(new Ellipse2D.Double(x, y, w, h));
        if (result == null) {
            return super.getContainedVillages();
        }
        return result;
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
        if (getXPos() < 0 || getYPos() < 0 || xPosEnd < 0 || yPosEnd < 0) {
            return;
        }
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(xPosEnd, yPosEnd);
        int x = (int) Math.rint((s.getX() < e.getX()) ? s.getX() : e.getX());
        int y = (int) Math.rint((s.getY() < e.getY()) ? s.getY() : e.getY());
        int w = (int) Math.rint(Math.abs(s.getX() - e.getX()));
        int h = (int) Math.rint(Math.abs(s.getY() - e.getY()));

        if (new Ellipse2D.Double(x, y, w, h).intersects(MapPanel.getSingleton().getBounds())) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            return;
        }
        //store properties
        Stroke sBefore = g2d.getStroke();
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawAlpha));
        g2d.setStroke(getStroke());
        checkShowMode(g2d, drawColor);

        if (filled) {
            g2d.fillOval(x, y, w, h);
        } else {
            g2d.drawOval(x, y, w, h);
        }
        //Shape clipBefore = g2d.getClip();
        // g2d.setClip(new Ellipse2D.Float(x, y, w, h));
        if (drawName) {
            g2d.setColor(getTextColor());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setFont(fBefore.deriveFont((float) getTextSize()));
            Rectangle2D textBounds = g2d.getFontMetrics().getStringBounds(getFormName(), g2d);
            g2d.drawString(getFormName(), (int) Math.rint((double) x + (double) w / 2 - textBounds.getWidth() / 2), (int) Math.rint((double) y + (double) h / 2 + textBounds.getHeight() / 2));
        }

        //restore properties
        g2d.setStroke(sBefore);
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
        // g2d.setClip(clipBefore);
    }

    public void renderPreview(Graphics2D g2d) {
        Point2D.Double s = new Point2D.Double(getXPos(), getYPos());
        Point2D.Double e = new Point2D.Double(xPosEnd, yPosEnd);
        int x = (int) ((s.x < e.x) ? s.x : e.x);
        int y = (int) ((s.y < e.y) ? s.y : e.y);
        int w = (int) Math.rint(Math.abs(s.x - e.x));
        int h = (int) Math.rint(Math.abs(s.y - e.y));

        if (new Ellipse2D.Double(x, y, w, h).intersects(MapPanel.getSingleton().getBounds())) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            return;
        }
        //store properties
        Stroke sBefore = g2d.getStroke();
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawAlpha));
        g2d.setStroke(getStroke());
        g2d.setColor(drawColor);

        if (filled) {
            g2d.fillOval(x, y, w, h);
        } else {
            g2d.drawOval(x, y, w, h);
        }

        if (drawName) {
            g2d.setColor(getTextColor());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setFont(fBefore.deriveFont((float) getTextSize()));
            Rectangle2D textBounds = g2d.getFontMetrics().getStringBounds(getFormName(), g2d);
            g2d.drawString(getFormName(), (int) Math.rint((double) x + (double) w / 2 - textBounds.getWidth() / 2), (int) Math.rint((double) y + (double) h / 2 + textBounds.getHeight() / 2));
        }

        //restore properties
        g2d.setStroke(sBefore);
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    @Override
    public java.awt.Rectangle getBounds() {
        Point2D.Double s = new Point2D.Double(getXPos(), getYPos());
        Point.Double e = new Point2D.Double(xPosEnd, yPosEnd);
        int x = (int) Math.round((s.getX() < e.getX()) ? s.getX() : e.getX());
        int y = (int) Math.round((s.getY() < e.getY()) ? s.getY() : e.getY());
        int w = (int) Math.round(Math.abs(s.getX() - e.getX()));
        int h = (int) Math.round(Math.abs(s.getY() - e.getY()));
        return new java.awt.Rectangle(x, y, w, h);
    }

    @Override
    protected String getFormXml() {
        StringBuilder b = new StringBuilder();
        b.append("<end x=\"").append(xPosEnd).append("\" y=\"").append(yPosEnd).append("\"/>\n");
        b.append("<drawColor r=\"").append(drawColor.getRed()).append("\" g=\"").append(drawColor.getGreen()).append("\" b=\"").append(drawColor.getBlue()).append("\" a=\"").append(drawAlpha).append("\"/>\n");
        b.append("<filled>").append(filled).append("</filled>\n");
        b.append("<stroke width=\"").append(strokeWidth).append("\"/>\n");
        b.append("<drawName>").append(drawName).append("</drawName>\n");
        return b.toString();
    }

    @Override
    public FORM_TYPE getFormType() {
        return FORM_TYPE.CIRCLE;
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
}
