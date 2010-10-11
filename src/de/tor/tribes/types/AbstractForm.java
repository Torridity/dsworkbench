/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import java.awt.Color;
import java.awt.Graphics2D;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public abstract class AbstractForm {

    private static Logger logger = Logger.getLogger("AbstractForm");
    private double xPos = 0;
    private double yPos = 0;
    private Color textColor = Color.BLACK;
    private float textAlpha = 1.0f;
    private String formName = "";
    private boolean visibleOnMap = false;
    private int textSize = 14;
    private boolean showMode = false;
    private long t = -1;
    private long showCount = 0;

    public abstract void renderForm(Graphics2D g2d);

    public static AbstractForm fromXml(Element e) {
        String formType = e.getAttributeValue("type");
        if (formType.equals("line")) {
            logger.debug("Loading 'line'");
            return Line.fromXml(e);
        } else if (formType.equals("arrow")) {
            logger.debug("Loading 'arrow'");
            return Arrow.fromXml(e);
        } else if (formType.equals("rectangle")) {
            logger.debug("Loading 'rectangle'");
            return Rectangle.fromXml(e);
        } else if (formType.equals("circle")) {
            logger.debug("Loading 'circle'");
            return Circle.fromXml(e);
        } else if (formType.equals("text")) {
            logger.debug("Loading 'text'");
            return Text.fromXml(e);
        } else if (formType.equals("freeform")) {
            logger.debug("Loading 'freeform'");
            return FreeForm.fromXml(e);
        } else {
            return null;
        }
    }

    public String toXml() {
        try {
            String xml = "<form type=\"" + getFormType() + "\">\n";
            xml += "<name><![CDATA[" + URLEncoder.encode(getFormName(), "UTF-8") + "]]></name>\n";
            xml += "<pos x=\"" + getXPos() + "\" y=\"" + getYPos() + "\"/>\n";// rot=\"" + getRotation() + "\"/>\n";
            xml += "<textColor r=\"" + getTextColor().getRed() + "\" g=\"" + getTextColor().getGreen() + "\" b=\"" + getTextColor().getBlue() + "\" a=\"" + getTextAlpha() + "\"/>\n";
            xml += "<textSize>" + getTextSize() + "</textSize>\n";
            xml += getFormXml();
            xml += "</form>\n";
            return xml;
        } catch (Exception e) {
            return "\n";
        }
    }

    protected abstract String getFormXml();

    public abstract java.awt.Rectangle getBounds();

    public ArrayList<Village> getContainedVillages() {
        java.awt.Rectangle bounds = getBounds();
        ArrayList<Village> v = new ArrayList<Village>();
        Village[][] villages = DataHolder.getSingleton().getVillages();
        for (int x = bounds.x; x <= bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y <= bounds.y + bounds.height; y++) {
                Village vi = villages[x][y];
                if (vi != null) {
                    v.add(vi);
                }
            }
        }

        return v;
    }

    public void checkShowMode(Graphics2D g2d, Color pColor) {
        if (isShowMode()) {
            if (t == -1) {
                t = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - t >= 500) {
                t = System.currentTimeMillis();
                g2d.setColor(new Color(255 - pColor.getRed(), 255 - pColor.getGreen(), 255 - pColor.getBlue()));
                showCount++;
            } else {
                g2d.setColor(pColor);
            }
            if (showCount == 5) {
                setShowMode(false);
                t = -1;
                showCount = 0;
            }
        } else {
            g2d.setColor(pColor);
        }
    }

    /**
     * @return the xPos
     */
    public double getXPos() {
        return xPos;
    }

    /**
     * @param xPos the xPos to set
     */
    public void setXPos(double xPos) {
        this.xPos = xPos;
    }

    /**
     * @return the yPos
     */
    public double getYPos() {
        return yPos;
    }

    /**
     * @param yPos the yPos to set
     */
    public void setYPos(double yPos) {
        this.yPos = yPos;
    }

    /**
     * @return the color
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * @param color the color to set
     */
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    /**
     * @return the formName
     */
    public abstract String getFormType();

    /**
     * @return the rotation
     */
    /*  public double getRotation() {
    return rotation;
    }*/
    /**
     * @param rotation the rotation to set
     */
    /*  public void setRotation(double rotation) {
    this.rotation = rotation;
    }*/
    /**
     * @return the alpha
     */
    public float getTextAlpha() {
        return textAlpha;
    }

    /**
     * @param alpha the alpha to set
     */
    public void setTextAlpha(float alpha) {
        this.textAlpha = alpha;
    }

    /**
     * @return the formName
     */
    public String getFormName() {
        return formName;
    }

    /**
     * @param formName the formName to set
     */
    public void setFormName(String formName) {
        this.formName = formName;
    }

    /**
     * @return the visibleOnMap
     */
    public boolean isVisibleOnMap() {
        return visibleOnMap;
    }

    /**
     * @param visibleOnMap the visibleOnMap to set
     */
    public void setVisibleOnMap(boolean visibleOnMap) {
        this.visibleOnMap = visibleOnMap;
    }

    @Override
    public String toString() {
        String s = getFormName();
        s += " (" + getFormType() + ")";
        return s;
    }

    /**
     * @return the size
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * @param size the size to set
     */
    public void setTextSize(int size) {
        this.textSize = size;
    }

    /**
     * @return the showMode
     */
    public boolean isShowMode() {
        return showMode;
    }

    /**
     * @param showMode the showMode to set
     */
    public void setShowMode(boolean showMode) {
        this.showMode = showMode;
    }
}
