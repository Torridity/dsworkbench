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

import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.util.GlobalOptions;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class Text extends AbstractForm {

    private java.awt.Rectangle mBounds = null;

    @Override
    public void formFromXml(Element e) {
    }

    @Override
    protected Element formToXml(String elementName) {
        return new Element(elementName);
    }

    @Override
    public boolean allowsBBExport() {
        return false;
    }

    @Override
    public String[] getReplacements(boolean pExtended) {
        return null;
    }

    @Override
    public void renderForm(Graphics2D g2d) {
        Font fBefore = g2d.getFont();
        g2d.setFont(fBefore.deriveFont((float) getTextSize()));
        java.awt.Rectangle mapBounds = MapPanel.getSingleton().getBounds();
        FontMetrics met = g2d.getFontMetrics();
        Rectangle2D bounds = met.getStringBounds(getFormName(), g2d);
        Point s = MapPanel.getSingleton().virtualPosToSceenPos(getXPos(), getYPos());
        mBounds = new java.awt.Rectangle((int) (bounds.getX() + s.x), (int) (bounds.getY() + s.y), (int) (bounds.getWidth()), (int) (bounds.getHeight()));
        java.awt.Rectangle virtBounds = new java.awt.Rectangle((int) (bounds.getX() + s.x), (int) (bounds.getY() + s.y), (int) (bounds.getWidth()), (int) (bounds.getHeight()));

        //calculate bounds
        int w = GlobalOptions.getSkin().getCurrentFieldWidth();//getImage(Skin.ID_DEFAULT_UNDERGROUND, zoom).getWidth(null);
        int h = GlobalOptions.getSkin().getCurrentFieldHeight();//getImage(Skin.ID_DEFAULT_UNDERGROUND, zoom).getHeight(null);
        mBounds = new java.awt.Rectangle((int) (getXPos()), (int) (getYPos()), (int) Math.rint(bounds.getWidth() / (double) w), (int) Math.rint(bounds.getHeight() / (double) h));

        if (mapBounds.intersects(virtBounds)) {
            setVisibleOnMap(true);
        } else {
            setVisibleOnMap(false);
            g2d.setFont(fBefore);
            return;
        }

        //save properties
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();

        //draw
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
        checkShowMode(g2d, getTextColor());
        g2d.drawString(getFormName(), s.x, s.y);
        //restore properties
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);
    }

    public void renderPreview(Graphics2D g2d, java.awt.Rectangle bounds) {
        //save properties
        Color cBefore = g2d.getColor();
        Composite coBefore = g2d.getComposite();
        Font fBefore = g2d.getFont();
        //draw
        g2d.setFont(fBefore.deriveFont((float) getTextSize()));
        Rectangle2D rect = g2d.getFontMetrics().getStringBounds(getFormName(), g2d);
        int x = (int) Math.rint(bounds.getWidth() / 2 - rect.getWidth() / 2);
        int y = (int) Math.rint(bounds.getHeight() / 2 + rect.getHeight() / 2);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTextAlpha()));
        g2d.setColor(getTextColor());
        g2d.drawString(getFormName(), x, y);
        //restore properties
        g2d.setColor(cBefore);
        g2d.setComposite(coBefore);
        g2d.setFont(fBefore);

    }

    @Override
    public java.awt.Rectangle getBounds() {
        if (mBounds == null) {
            //return some default value to avoid NPE in model
            return new java.awt.Rectangle(0, 0, 0, 0);
        }
        return mBounds;
    }

    @Override
    public FORM_TYPE getFormType() {
        return FORM_TYPE.TEXT;
    }
}
