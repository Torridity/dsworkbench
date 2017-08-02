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
package de.tor.tribes.ui.renderer.map;

import de.tor.tribes.types.Note;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.note.NoteManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

/**
 *
 * @author Torridity
 */
public class AnimatedVillageInfoRenderer {

    private int iDiameter = 0;
    private boolean pRise = true;
    private boolean bFinished = false;
    private Village mVillage = null;
    private Rectangle mCurrentLocation = null;
    private int deltaDeg = 0;

    public AnimatedVillageInfoRenderer(Village pVillage) {
        mVillage = pVillage;
    }

    public Village getVillage() {
        return mVillage;
    }

    public boolean isFinished() {
        return bFinished;
    }

    public void update(Village pCurrentVillage, Rectangle pCurrentLocation, Graphics2D g2d) {
        if (pCurrentLocation == null || bFinished) {
            bFinished = true;
            return;
        }
        mCurrentLocation = (Rectangle) pCurrentLocation.clone();
        if (pRise) {
            if (iDiameter + 10 < 61) {
                iDiameter += 10;
            }
        } else {
            iDiameter -= 10;
            if (iDiameter <= 0) {
                bFinished = true;
                iDiameter = 0;
            }
        }
        renderTroopInfo(pCurrentVillage, g2d);
        renderNoteInfo(pCurrentVillage, g2d);
    }

    private void renderTroopInfo(Village pCurrentVillage, Graphics2D g2d) {
        pRise = mVillage != null && mVillage.equals(pCurrentVillage);

        VillageTroopsHolder holder = null;
        int centerX = (int) Math.floor(mCurrentLocation.getCenterX());
        int centerY = (int) Math.floor(mCurrentLocation.getCenterY());
        int halfDiameter = (int) Math.floor(iDiameter / 2.0);

        if (GlobalOptions.getProperties().getBoolean("include.support")) {
            holder = TroopsManager.getSingleton().getTroopsForVillage(mVillage);
        } else {
            holder = TroopsManager.getSingleton().getTroopsForVillage(mVillage, TroopsManager.TROOP_TYPE.OWN);
        }

        if (mVillage != null && holder != null) {
            double offValue = holder.getOffValue();
            double defArchValue = holder.getDefArcherValue();
            double defCavValue = holder.getDefCavalryValue();
            double defValue = holder.getDefValue();
            double fightValueIn = offValue + defValue + defArchValue + defCavValue;
            Color before = g2d.getColor();
            Composite cb = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            if (fightValueIn == 0) {
                g2d.setColor(Constants.DS_BACK_LIGHT);
                g2d.fillOval(centerX - halfDiameter, centerY - halfDiameter, iDiameter, iDiameter);
            } else {
                double percOff = offValue / fightValueIn;
                double percDef = defValue / fightValueIn;
                double percDefCav = defCavValue / fightValueIn;
                double percDefArch = defArchValue / fightValueIn;
                int degOff = 0;
                int degDef = 0;
                int degDefCav = 0;
                if (percOff > 0) {
                    //draw off arc
                    degOff = (int) Math.rint(360 * percOff);
                    g2d.setColor(Color.RED);
                    g2d.fillArc(centerX - halfDiameter, centerY - halfDiameter, iDiameter, iDiameter, 0, degOff);
                }
                if (percDef > 0) {
                    //draw def arc
                    degDef = (int) Math.rint(360 * percDef);
                    g2d.setColor(Color.GREEN);
                    g2d.fillArc(centerX - halfDiameter, centerY - halfDiameter, iDiameter, iDiameter, degOff, degDef);
                }
                if (percDefCav > 0) {
                    //draw def cav arc
                    degDefCav = (int) Math.rint(360 * percDefCav);
                    g2d.setColor(Color.YELLOW);
                    g2d.fillArc(centerX - halfDiameter, centerY - halfDiameter, iDiameter, iDiameter, degOff + degDef, degDefCav);
                }
                if (percDefArch > 0) {
                    //draw def cav arc
                    g2d.setColor(Color.ORANGE.darker());
                    g2d.fillArc(centerX - halfDiameter, centerY - halfDiameter, iDiameter, iDiameter, degOff + degDef + degDefCav, 360 - (degOff + degDef + degDefCav));
                }

                renderTroopInfoLegend(centerX, centerY, offValue, defValue, defCavValue, defArchValue, g2d);

            }
            g2d.setColor(before);
            g2d.setComposite(cb);
        } else {
            g2d.setColor(Constants.DS_BACK_LIGHT);
            g2d.fillOval(centerX - halfDiameter, centerY - halfDiameter, iDiameter, iDiameter);
            if (iDiameter == 60) {
                g2d.setColor(Color.BLACK);
                Rectangle2D bounds = g2d.getFontMetrics().getStringBounds("Keine", g2d);
                g2d.drawString("Keine", centerX - Math.round(bounds.getWidth() / 2), Math.round(centerY));
                bounds = g2d.getFontMetrics().getStringBounds("Info", g2d);
                g2d.drawString("Info", centerX - Math.round(bounds.getWidth() / 2), Math.round(centerY + bounds.getHeight()));
            }
        }
    }

    private void renderTroopInfoLegend(int x, int y, double offValue, double defValue, double defCavValue, double defArchValue, Graphics2D g2d) {
        if (iDiameter < 60) {
            //no legend until finished rising
            return;
        }
        Font f = g2d.getFont();
        g2d.setColor(Constants.DS_BACK_LIGHT);
        g2d.drawLine(x, y, x + 60, y - 80);
        g2d.drawLine(x + 60, y - 80, x + 80, y - 80);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String offLegendValue = nf.format(offValue);
        Rectangle2D offBounds = g2d.getFontMetrics().getStringBounds(offLegendValue, g2d);
        String defLegendValue = nf.format(defValue);
        Rectangle2D defBounds = g2d.getFontMetrics().getStringBounds(defLegendValue, g2d);
        String defCavLegendValue = nf.format(defCavValue);
        Rectangle2D defCavBounds = g2d.getFontMetrics().getStringBounds(defCavLegendValue, g2d);
        String defArchLegendValue = nf.format(defArchValue);
        Rectangle2D defArchBounds = g2d.getFontMetrics().getStringBounds(defArchLegendValue, g2d);

        double width = Math.max(Math.max(Math.max(offBounds.getWidth(), defBounds.getWidth()), defCavBounds.getWidth()), defArchBounds.getWidth());
        width += 10 + 18;
        double height = 64 + 10;
        int textHeight = (int) Math.round(offBounds.getHeight());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
        g2d.fillRect(x + 80, y - 80, (int) Math.round(width), (int) Math.round(height));
        g2d.setColor(Color.BLACK);
        g2d.setFont(f.deriveFont(Font.BOLD));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_BARRACkS), x + 80 + 2, y - 80 + 2, null);
        g2d.drawString(offLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + textHeight);
        g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_ALLY), x + 80 + 2, y - 80 + 2 + 16 + 2, null);
        g2d.drawString(defLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + 16 + 2 + textHeight);
        g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_DEF_CAV), x + 80 + 2, y - 80 + 2 + 16 + 2 + 16 + 2, null);
        g2d.drawString(defCavLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + 16 + 2 + 16 + 2 + textHeight);
        g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_DEF_ARCH), x + 80 + 2, y - 80 + 2 + 16 + 2 + 16 + 2 + 16 + 2, null);
        g2d.drawString(defArchLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + 16 + 2 + 16 + 2 + 16 + 2 + textHeight);

        g2d.setFont(f);
    }

    private void renderNoteInfo(Village pCurrentVillage, Graphics2D g2d) {

        pRise = mVillage != null && mVillage.equals(pCurrentVillage);
        int noteCount = NoteManager.getSingleton().getNotesForVillage(mVillage).size();
        double deg = 0;
        if (noteCount != 0) {
            deg = 360 / noteCount;
        }
        int cnt = 0;
        int centerX = (int) Math.floor(mCurrentLocation.getCenterX());
        int centerY = (int) Math.floor(mCurrentLocation.getCenterY());

        int iNoteDiameter = 0;
        if (iDiameter >= 40) {
            iNoteDiameter = 40;
        }

        if (iNoteDiameter == 40) {
            Color c = g2d.getColor();
            Composite com = g2d.getComposite();
            Area a = new Area();

            /*a.add(new Area(new Ellipse2D.Double(centerX - 50, centerY - 50, 100, 100)));
            a.subtract(new Area(new Ellipse2D.Double(centerX - 30, centerY - 30, 60, 60)));*/
            a.add(new Area(new Ellipse2D.Double(centerX - (int) Math.round((40.0 + iDiameter) / 2), centerY - (int) Math.round((40.0 + iDiameter) / 2), 40 + iDiameter, 40 + iDiameter)));
            a.subtract(new Area(new Ellipse2D.Double(centerX - (int) Math.round(iDiameter / 2.0), centerY - (int) Math.round(iDiameter / 2.0), iDiameter, iDiameter)));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2d.setColor(Constants.DS_BACK_LIGHT);
            g2d.fill(a);
            g2d.setComposite(com);
            g2d.setColor(Constants.DS_BACK);
            g2d.drawOval(centerX - (int) Math.round((40.0 + iDiameter) / 2), centerY - (int) Math.round((40.0 + iDiameter) / 2), 40 + iDiameter, 40 + iDiameter);
            g2d.drawOval(centerX - (int) Math.round(iDiameter / 2.0), centerY - (int) Math.round(iDiameter / 2.0), iDiameter, iDiameter);
            g2d.setColor(c);
        }
        for (Note note : NoteManager.getSingleton().getNotesForVillage(mVillage)) {
            //take next degree
            double cd = cnt * deg + deltaDeg;
            int noteX = (int) Math.rint(centerX + iNoteDiameter * Math.cos(2 * Math.PI * cd / 360) - 8);
            int noteY = (int) Math.rint(centerY + iNoteDiameter * Math.sin(2 * Math.PI * cd / 360) - 8);
            int noteIcon = note.getMapMarker();
            //   g2d.copyArea(noteIcon * 32, pCopyPosition + 68, 32, 32, noteX - noteIcon * 32, noteY - pCopyPosition - 68);
            g2d.drawImage(ImageManager.getNoteIcon(noteIcon).getScaledInstance(16, 16, BufferedImage.SCALE_DEFAULT), noteX, noteY, null);
            cnt++;
        }
        deltaDeg += 1;
        if (deltaDeg == 360) {
            deltaDeg = 0;
        }


    }
}
