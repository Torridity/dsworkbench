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
package de.tor.tribes.ui.panels;

import de.tor.tribes.types.drawing.Circle;
import de.tor.tribes.types.drawing.FreeForm;
import de.tor.tribes.types.drawing.Line;
import de.tor.tribes.types.drawing.Arrow;
import de.tor.tribes.types.drawing.Rectangle;
import de.tor.tribes.types.drawing.Text;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * @author Charon
 */
public class FormSamplePanel extends javax.swing.JPanel {

    private Color mDrawColor = Color.WHITE;
    private Color mTextColor = Color.BLACK;
    private float fDrawTransparency = 0.0f;
    private float fTextTransparency = 0.0f;
    private float fStrokeWidth = 1.0f;
    private int fTextSize = 14;
    //type 0-4: 0=none, 1=end, 2=start, 3=both
    private boolean drawStartArrow = false;
    private boolean drawEndArrow = false;
    private boolean bFill = false;
    private boolean drawText = false;
    private String sText = "";
    private BufferedImage sampleTexture = null;
    //type 0-4: 0=line, 1=rect, 2=circle, 3=text, 4=freeform
    private int type = 0;
    private int roundBorders = 0;
    private float tolerance = 0.5f;

    /** Creates new form SamplePanel */
    public FormSamplePanel() {
        initComponents();
        try {
            sampleTexture = ImageIO.read(new File("graphics/skins/default/v6.png"));
        } catch (Exception ignored) {
        }
    }

    public void setDrawColor(Color c) {
        mDrawColor = c;
    }

    public void setDrawTransparency(float t) {
        fDrawTransparency = t;
    }

    public void setDrawText(boolean c) {
        drawText = c;
    }

    public void setRoundBorders(int c) {
        roundBorders = c;
    }

    public void setTextColor(Color c) {
        mTextColor = c;
    }

    public void setTextTransparency(float t) {
        fTextTransparency = t;
    }

    public void setTextSize(int t) {
        fTextSize = t;
    }

    public void setStrokeWidth(float s) {
        fStrokeWidth = s;
    }

    public void setFill(boolean f) {
        bFill = f;
    }

    public void setText(String c) {
        sText = c;
    }

    public void drawStartArrow(boolean v) {
        drawStartArrow = v;
    }

    public void drawEndArrow(boolean v) {
        drawEndArrow = v;
    }

    public void setTolerance(float v) {
        tolerance = v;
    }

    public float getTolerance() {
        return tolerance;
    }

    public void setType(int v) {
        type = v;
    }

    public BufferedImage createSample() {
        BufferedImage result = new BufferedImage(6 * GlobalOptions.getSkin().getBasicFieldWidth(), 2 * GlobalOptions.getSkin().getBasicFieldHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) result.getGraphics();
        if (!GlobalOptions.getSkin().isMinimapSkin()) {
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, result.getWidth(), result.getHeight());
            for (int i = Skin.ID_V1; i <= Skin.ID_V6; i++) {
                Image image = GlobalOptions.getSkin().getImage(i, 1.0f);
                g2d.drawImage(image, (i - Skin.ID_V1) * image.getWidth(null), 0, this);
            }
            for (int i = Skin.ID_V1_LEFT; i <= Skin.ID_V6_LEFT; i++) {
                Image image = GlobalOptions.getSkin().getImage(i, 1.0f);
                g2d.drawImage(image, (i - Skin.ID_V1_LEFT) * image.getWidth(null), image.getHeight(null), this);
            }
            g2d.dispose();
            return result;
        } else {
            g2d.setColor(Constants.DS_BACK);
            g2d.fillRect(0, 0, result.getWidth(), result.getHeight());
            g2d.dispose();
            return result;
        }
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(mDrawColor);
        Paint p = g2d.getPaint();
        BufferedImage sample = createSample();
        g2d.setPaint(new TexturePaint(sample, new Rectangle2D.Double(0, 0, sample.getWidth(), sample.getHeight())));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setPaint(p);
        switch (type) {
            case 0: {
                //preview line
                Line l = new Line();
                l.setDrawColor(mDrawColor);
                l.setDrawAlpha(fDrawTransparency);
                l.setTextColor(mTextColor);
                l.setTextAlpha(fTextTransparency);
                l.setStartArrow(drawStartArrow);
                l.setEndArrow(drawEndArrow);
                l.setTextSize(fTextSize);
                l.setDrawName(drawText);
                l.setFormName(sText);
                l.setStrokeWidth(fStrokeWidth);
                l.setXPos(30);
                l.setYPos(getHeight() / 2);
                l.setXPosEnd(getWidth() - 30);
                l.setYPosEnd(getHeight() / 2);
                l.renderPreview(g2d);
                break;
            }
            case 1: {
                //preview rect
                Rectangle l = new Rectangle();
                l.setDrawColor(mDrawColor);
                l.setDrawAlpha(fDrawTransparency);
                l.setTextColor(mTextColor);
                l.setTextAlpha(fTextTransparency);
                l.setFilled(bFill);
                l.setTextSize(fTextSize);
                l.setDrawName(drawText);
                l.setFormName(sText);
                l.setStrokeWidth(fStrokeWidth);
                l.setXPos(10);
                l.setYPos(10);
                l.setXPosEnd(getWidth() - 10);
                l.setYPosEnd(getHeight() - 10);
                l.setRounding(roundBorders);
                l.renderPreview(g2d);
                break;
            }
            case 2: {
                //preview circle
                Circle l = new Circle();
                l.setDrawColor(mDrawColor);
                l.setDrawAlpha(fDrawTransparency);
                l.setTextColor(mTextColor);
                l.setTextAlpha(fTextTransparency);
                l.setFilled(bFill);
                l.setTextSize(fTextSize);
                l.setDrawName(drawText);
                l.setFormName(sText);
                l.setStrokeWidth(fStrokeWidth);
                l.setXPos(10);
                l.setYPos(10);
                l.setXPosEnd(getWidth() - 10);
                l.setYPosEnd(getHeight() - 10);
                l.renderPreview(g2d);
                break;
            }
            case 3: {
                //preview text
                Text l = new Text();
                l.setTextColor(mTextColor);
                l.setTextAlpha(fTextTransparency);
                l.setTextSize(fTextSize);
                l.setFormName(sText);
                l.renderPreview(g2d, getBounds());
                break;
            }
            case 4: {
                //preview freeform
                FreeForm l = new FreeForm();
                l.setDrawColor(mDrawColor);
                l.setDrawAlpha(fDrawTransparency);
                l.setTextColor(mTextColor);
                l.setTextAlpha(fTextTransparency);
                l.setFilled(bFill);
                l.setTextSize(fTextSize);
                l.setDrawName(drawText);
                l.setFormName(sText);
                l.setStrokeWidth(fStrokeWidth);
                l.setXPos(getWidth() / 2);
                l.setYPos(10);
                l.addPoint(new Point2D.Double(getWidth() - 10, getHeight() - 10));
                l.addPoint(new Point2D.Double(10, getHeight() - 10));
                l.setTolerance(tolerance);
                l.renderPreview(g2d);
                break;
            }
            case 5: {
                //preview arrow
                Arrow l = new Arrow();
                l.setDrawColor(mDrawColor);
                l.setDrawAlpha(fDrawTransparency);
                l.setTextColor(mTextColor);
                l.setTextAlpha(fTextTransparency);
                l.setTextSize(fTextSize);
                l.setDrawName(drawText);
                l.setFormName(sText);
                l.setFilled(bFill);
                l.setStrokeWidth(fStrokeWidth);
                l.setXPos(30);
                l.setYPos(getHeight() / 2);
                l.setXPosEnd(getWidth() - 30);
                l.setYPosEnd(getHeight() / 2);
                l.renderPreview(g2d);
                break;
            }

        }

        /*
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(mColor);
        Composite before = g2d.getComposite();
        Paint p = g2d.getPaint();
        g2d.setPaint(new TexturePaint(sampleTexture, new Rectangle2D.Double(0, 0, sampleTexture.getWidth(), sampleTexture.getHeight())));
        g2d.fillRect(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
        g2d.setPaint(p);
        Stroke stro = g2d.getStroke();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fTransparency));
        if (bFill) {
        g2d.fillRect(getBounds().x + 10, getBounds().y + 10, getBounds().width - 20, getBounds().height - 20);
        } else {
        g2d.setStroke(new BasicStroke(fStrokeWidth));
        g2d.drawRect(getBounds().x + 10, getBounds().y + 10, getBounds().width - 20, getBounds().height - 20);
        g2d.setStroke(stro);
        }
        if (sText.length() > 0) {
        Font fb = g2d.getFont();
        g2d.setFont(fb.deriveFont(fStrokeWidth));
        g2d.drawString(sText, 20, getHeight() - 20);
        g2d.setFont(fb);
        }
        g2d.setComposite(before);*/
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
