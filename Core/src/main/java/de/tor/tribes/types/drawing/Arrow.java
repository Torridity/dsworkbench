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
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.net.URLDecoder;
import java.util.List;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class Arrow extends AbstractForm {

    private double xPosEnd = -1;
    private double yPosEnd = -1;
    private GeneralPath path = null;
    private boolean filled = false;
    private Color drawColor = Color.WHITE;
    private float drawAlpha = 1.0f;
    private float strokeWidth = 1.0f;
    private boolean drawName = true;

    public Arrow() {
        super();
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
    protected String getFormXml() {
        StringBuilder b = new StringBuilder();
        b.append("<end x=\"").append(xPosEnd).append("\" y=\"").append(yPosEnd).append("\"/>\n");
        b.append("<filled>").append(filled).append("</filled>\n");
        b.append("<drawColor r=\"").append(drawColor.getRed()).append("\" g=\"").append(drawColor.getGreen()).append("\" b=\"").append(drawColor.getBlue()).append("\" a=\"").append(drawAlpha).append("\"/>\n");
        b.append("<stroke width=\"").append(strokeWidth).append("\"/>\n");
        b.append("<drawName>").append(drawName).append("</drawName>\n");
        return b.toString();
    }

    @Override
    public FORM_TYPE getFormType() {
        return FORM_TYPE.ARROW;
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

    @Override
    public void renderForm(Graphics2D g2d) {
        Point2D.Double s = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        Point2D.Double e = MapPanel.getSingleton().virtualPosToSceenPosDouble(xPosEnd, yPosEnd);
        if (xPosEnd == -1 && yPosEnd == -1) {
            e = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        }
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

        double h = Math.abs(s.y - e.y);
        double c = s.distance(e);
        double a = Math.asin(h / c);

        /*
         * x > 0, y > 0: quadrant I.
        x < 0, y > 0: quadrant II.
        x < 0, y < 0: quadrant III
        x > 0, y < 0: quadrant IV
         */
        path = new GeneralPath();
        path.moveTo(0, -10);
        path.lineTo(80, -10);
        path.lineTo(80, -20);
        path.lineTo(100, 0);
        path.lineTo(80, 20);
        path.lineTo(80, 10);
        path.lineTo(0, 10);
        path.closePath();

        double rot = 0;

        if (e.x > s.x && e.y >= s.y) {
            rot = Math.toDegrees(a);
        } else if (e.x <= s.x && e.y >= s.y) {
            rot = 180 - Math.toDegrees(a);
        } else if (e.x >= s.x && e.y <= s.y) {
            rot = 360 - Math.toDegrees(a);
        } else {
            rot = 180 + Math.toDegrees(a);
        }

        a = Math.toRadians(rot);
        AffineTransform trans = AffineTransform.getScaleInstance(c / 100.0, c / 210.0);
        path.transform(trans);
        trans = AffineTransform.getTranslateInstance(path.getBounds2D().getX(), 0);
        path.transform(trans);
        trans = AffineTransform.getRotateInstance(a, 0, 0);
        path.transform(trans);
        trans = AffineTransform.getTranslateInstance(s.x, s.y);
        path.transform(trans);

        if (filled) {
            g2d.fill(path);
        } else {
            g2d.draw(path);
        }
        drawDecoration(s, e, g2d);
        //reset properties
        g2d.setStroke(before);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
        g2d.setColor(cBefore);
    }

    public void renderPreview(Graphics2D g2d) {
        Point2D.Double s = new Point2D.Double(getXPos(), getYPos());
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
        path = new GeneralPath();
        path.moveTo(getXPos(), getYPos() - 10);
        path.lineTo(getXPos() + 80, getYPos() - 10);
        path.lineTo(getXPos() + 80, getYPos() - 20);
        path.lineTo(getXPos() + 100, getYPos());
        path.lineTo(getXPos() + 80, getYPos() + 20);
        path.lineTo(getXPos() + 80, getYPos() + 10);
        path.lineTo(getXPos() + 0, getYPos() + 10);
        path.closePath();

        if (filled) {
            g2d.fill(path);
        } else {
            g2d.draw(path);
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
        if (xPosEnd == -1 && yPosEnd == -1) {
            e = MapPanel.getSingleton().virtualPosToSceenPosDouble(getXPos(), getYPos());
        }
        double h = Math.abs(s.y - e.y);
        double c = s.distance(e);
        double a = Math.asin(h / c);

        /*
         * x > 0, y > 0: quadrant I.
        x < 0, y > 0: quadrant II.
        x < 0, y < 0: quadrant III
        x > 0, y < 0: quadrant IV
         */
        path = new GeneralPath();
        path.moveTo(0, -10);
        path.lineTo(80, -10);
        path.lineTo(80, -20);
        path.lineTo(100, 0);
        path.lineTo(80, 20);
        path.lineTo(80, 10);
        path.lineTo(0, 10);
        path.closePath();

        double rot = 0;

        if (e.x > s.x && e.y >= s.y) {
            rot = Math.toDegrees(a);
        } else if (e.x <= s.x && e.y >= s.y) {
            rot = 180 - Math.toDegrees(a);
        } else if (e.x >= s.x && e.y <= s.y) {
            rot = 360 - Math.toDegrees(a);
        } else {
            rot = 180 + Math.toDegrees(a);
        }

        a = Math.toRadians(rot);
        AffineTransform trans = AffineTransform.getScaleInstance(c / 100.0, c / 210.0);
        path.transform(trans);
        trans = AffineTransform.getTranslateInstance(path.getBounds2D().getX(), 0);
        path.transform(trans);
        trans = AffineTransform.getRotateInstance(a, 0, 0);
        path.transform(trans);
        trans = AffineTransform.getTranslateInstance(s.x, s.y);
        path.transform(trans);



        List<Village> result = MapPanel.getSingleton().getVillagesInShape(path);
        if (result == null) {
            return super.getContainedVillages();
        }
        return result;
    }

    private void drawDecoration(Point2D.Double s, Point2D.Double e, Graphics2D g2d) {
        if (drawName) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
            g2d.setColor(getTextColor());
            g2d.drawString(getFormName(), (int) s.x, (int) s.y);
        }
    }

    public BasicStroke getStroke() {
        float w = (float) (strokeWidth / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
        return new BasicStroke(w);
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
}
