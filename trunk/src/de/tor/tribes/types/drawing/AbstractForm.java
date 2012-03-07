/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types.drawing;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import java.awt.Color;
import java.awt.Graphics2D;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public abstract class AbstractForm extends ManageableType implements BBSupport {

    private final static String[] VARIABLES = new String[]{"%Name%", "%START_X%", "%START_Y%", "%WIDTH%", "%HEIGHT%", "%END_X%", "%END_Y%", "%COLOR%", "%VILLAGE_LIST%"};
    private final static String STANDARD_TEMPLATE = "%Name% (%START_X%|%START_Y% bis %END_X%|%END_Y%)\nEnthaltene Dörfer:\n%VILLAGE_LIST%";

    public abstract boolean allowsBBExport();

    @Override
    public String[] getBBVariables() {
        return VARIABLES;
    }

    @Override
    public String getStandardTemplate() {
        return STANDARD_TEMPLATE;
    }

    public static enum FORM_TYPE {

        LINE, ARROW, RECTANGLE, CIRCLE, TEXT, FREEFORM
    }
    private static Logger logger = Logger.getLogger("AbstractForm");
    private double xPos = 0;
    private double yPos = 0;
    private Color textColor = Color.BLACK;
    private float textAlpha = 1.0f;
    private String formName = "Kein Name";
    private boolean visibleOnMap = false;
    private int textSize = 14;
    private boolean showMode = false;
    private long t = -1;
    private long showCount = 0;

    public abstract void renderForm(Graphics2D g2d);

    /**
     *
     * @param e
     * @return
     */
    public static AbstractForm fromXml(Element e) {
        String formType = e.getAttributeValue("type");
        if (formType.equals("line")) {
            logger.debug("Loading 'line'");
            Line l = new Line();
            l.loadFromXml(e);
            return l;
        } else if (formType.equals("arrow")) {
            logger.debug("Loading 'arrow'");
            Arrow a = new Arrow();
            a.loadFromXml(e);
            return a;
        } else if (formType.equals("rectangle")) {
            logger.debug("Loading 'rectangle'");
            Rectangle r = new Rectangle();
            r.loadFromXml(e);
            return r;
        } else if (formType.equals("circle")) {
            logger.debug("Loading 'circle'");
            Circle c = new Circle();
            c.loadFromXml(e);
            return c;
        } else if (formType.equals("text")) {
            logger.debug("Loading 'text'");
            Text t = new Text();
            t.loadFromXml(e);
            return t;
        } else if (formType.equals("freeform")) {
            logger.debug("Loading 'freeform'");
            FreeForm f = new FreeForm();
            f.loadFromXml(e);
            return f;
        } else {
            return null;
        }
    }

    public FORM_TYPE getTypeForString(String formType) {
        if (formType.equals("line")) {
            return FORM_TYPE.LINE;
        } else if (formType.equals("arrow")) {
            return FORM_TYPE.ARROW;
        } else if (formType.equals("rectangle")) {
            return FORM_TYPE.RECTANGLE;
        } else if (formType.equals("circle")) {
            return FORM_TYPE.CIRCLE;
        } else if (formType.equals("text")) {
            return FORM_TYPE.TEXT;
        } else if (formType.equals("freeform")) {
            return FORM_TYPE.FREEFORM;
        } else {
            return null;
        }
    }

    public String getTypeAsString(FORM_TYPE formType) {
        switch (formType) {
            case ARROW:
                return "arrow";
            case CIRCLE:
                return "circle";
            case FREEFORM:
                return "freeform";
            case LINE:
                return "line";
            case RECTANGLE:
                return "rectangle";
            default:
                return "text";
        }
    }

    @Override
    public String toXml() {
        try {
            StringBuilder b = new StringBuilder();
            b.append("<form type=\"").append(getTypeAsString(getFormType())).append("\">\n");
            b.append("<name><![CDATA[").append(URLEncoder.encode(getFormName(), "UTF-8")).append("]]></name>\n");
            b.append("<pos x=\"").append(getXPos()).append("\" y=\"").append(getYPos()).append("\"/>\n");// rot=\"" + getRotation() + "\"/>\n");
            b.append("<textColor r=\"").append(getTextColor().getRed()).append("\" g=\"").append(getTextColor().getGreen()).append("\" b=\"").append(getTextColor().getBlue()).append("\" a=\"").append(getTextAlpha()).append("\"/>\n");
            b.append("<textSize>").append(getTextSize()).append("</textSize>\n");
            b.append(getFormXml());
            b.append("</form>\n");
            return b.toString();
        } catch (Exception e) {
            return "\n";
        }
    }

    protected abstract String getFormXml();

    public abstract java.awt.Rectangle getBounds();

    public List<Village> getContainedVillages() {
        java.awt.Rectangle bounds = getBounds();
        List<Village> v = new ArrayList<Village>();
        for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
                try {
                    Village vi = DataHolder.getSingleton().getVillages()[x][y];
                    if (vi != null) {
                        v.add(vi);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
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
    public abstract FORM_TYPE getFormType();

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
        if (formName == null || formName.length() < 1) {
            this.formName = "Kein Name";
        }
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
        return getFormName() + " (" + getTypeAsString(getFormType()) + ")";
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

    @Override
    public String getElementIdentifier() {
        return "";
    }

    @Override
    public String getElementGroupIdentifier() {
        return "";
    }

    @Override
    public String getGroupNameAttributeIdentifier() {
        return "";
    }
}