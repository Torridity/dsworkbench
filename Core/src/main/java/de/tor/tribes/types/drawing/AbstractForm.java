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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.BBSupport;
import java.awt.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

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

    public enum FORM_TYPE {
        LINE, ARROW, RECTANGLE, CIRCLE, TEXT, FREEFORM
    }
    private static Logger logger = LogManager.getLogger("AbstractForm");
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

    public static AbstractForm fromXml(Element e) {
        FORM_TYPE type = FORM_TYPE.valueOf(e.getAttributeValue("type"));
        AbstractForm form;
        switch (type) {
            case LINE:
                logger.debug("Loading 'line'");
                form = new Line();
                break;
            case ARROW:
                logger.debug("Loading 'arrow'");
                form = new Arrow();
                break;
            case RECTANGLE:
                logger.debug("Loading 'rectangle'");
                form = new Rectangle();
                break;
            case CIRCLE:
                logger.debug("Loading 'circle'");
                form= new Circle();
                break;
            case TEXT:
                logger.debug("Loading 'text'");
                form = new Text();
                break;
            case FREEFORM:
                logger.debug("Loading 'freeform'");
                form = new FreeForm();
                break;
            default:
                return null;
        }
        form.loadFromXml(e);
        return form;
    }
    
    @Override
    public final void loadFromXml(Element e) {
        try {
            setFormName(URLDecoder.decode(e.getChild("name").getTextTrim(), "UTF-8"));
            Element elem = e.getChild("pos");
            setXPos(Double.parseDouble(elem.getAttributeValue("x")));
            setYPos(Double.parseDouble(elem.getAttributeValue("y")));
            elem = e.getChild("textColor");
            setTextColor(new Color(Integer.parseInt(elem.getAttributeValue("r")),
                    Integer.parseInt(elem.getAttributeValue("g")),
                    Integer.parseInt(elem.getAttributeValue("b"))));
            setTextAlpha(Float.parseFloat(elem.getAttributeValue("a")));
            setTextSize(Integer.parseInt(e.getChild("textSize").getTextTrim()));
            setTextSize(Integer.parseInt(e.getChildText("textSize")));
            
            formFromXml(e.getChild("extra"));
        } catch (IOException ignored) {
        }
    }
    
    protected abstract void formFromXml(Element elm);

    @Override
    public Element toXml(String elementName) {
        Element form = new Element(elementName);
        try {
            form.setAttribute("type", getFormType().name());
            form.addContent(new Element("name").setText("<![CDATA[" + URLEncoder.encode(formName, "UTF-8") + "]]>"));
            form.addContent(new Element("pos").setAttribute("x", Double.toString(getXPos()))
                    .setAttribute("y", Double.toString(getYPos())));
            
            Element color = new Element("textColor");
            color.setAttribute("r", Integer.toString(textColor.getRed()));
            color.setAttribute("g", Integer.toString(textColor.getGreen()));
            color.setAttribute("b", Integer.toString(textColor.getBlue()));
            color.setAttribute("a", Integer.toString(textColor.getAlpha()));
            form.addContent(color);
            form.addContent(new Element("textSize").setText(Integer.toString(textSize)));
            form.addContent(formToXml("extra"));
        } catch (Exception ignored) {
        }
        return form;
    }

    protected abstract Element formToXml(String elementName);

    public abstract java.awt.Rectangle getBounds();

    public List<Village> getContainedVillages() {
        java.awt.Rectangle bounds = getBounds();
        List<Village> v = new ArrayList<>();
        for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
                try {
                    Village vi = DataHolder.getSingleton().getVillages()[x][y];
                    if (vi != null) {
                        v.add(vi);
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }
            }
        }
        return v;
    }

    public void checkShowMode(Graphics2D g2d, Color pColor) {
        if (showMode) {
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
                this.showMode = false;
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
        return formName + " (" + getFormType().name() + ")";
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
