/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Hashtable;

public class TroopDensityLayerRenderer extends AbstractBufferedLayerRenderer {

    private BufferedImage mLayer = null;
    private Point mapPos = null;
    private boolean shouldReset = true;
    private Hashtable<Village, TroopAnimator> animators = new Hashtable<Village, TroopAnimator>();

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (!pSettings.isLayerVisible()) {
            return;
        }
        if (pSettings.getFieldHeight() < 15) {
            //skip drawing and schedule reset for next valid draw
            shouldReset = true;
            return;
        }
        if (shouldReset) {
            setFullRenderRequired(true);
            shouldReset = false;
            mapPos = null;
            if (mLayer != null) {
                if (MapPanel.getSingleton().getWidth() > mLayer.getWidth()
                        || MapPanel.getSingleton().getWidth() < mLayer.getWidth() - 100
                        || MapPanel.getSingleton().getHeight() > mLayer.getHeight()
                        || MapPanel.getSingleton().getHeight() < mLayer.getHeight() - 100
                        || MapPanel.getSingleton().getWidth() < pSettings.getFieldWidth() * pSettings.getVisibleVillages().length
                        || MapPanel.getSingleton().getHeight() < pSettings.getFieldHeight() * pSettings.getVisibleVillages()[0].length) {
                    mLayer.flush();
                    mLayer = null;
                }
            }
        }
        Graphics2D g2d = null;
        if (isFullRenderRequired()) {
            if (mLayer == null) {
                mLayer = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), BufferedImage.BITMASK);
                g2d = mLayer.createGraphics();
            } else {
                g2d = (Graphics2D) mLayer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, mLayer.getWidth(), mLayer.getHeight());
                g2d.setComposite(c);
            }
            pSettings.setRowsToRender(pSettings.getVisibleVillages()[0].length);
        } else {
//copy existing data to new location
            g2d = (Graphics2D) mLayer.getGraphics();
            performCopy(pSettings, g2d);
        }
        ImageUtils.setupGraphics(g2d);
//Set new bounds
        BufferedImage img = renderTroopRows(pSettings);
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        if (pSettings.getRowsToRender() < 0) {
            trans.setToTranslation(0, (pSettings.getVisibleVillages()[0].length + pSettings.getRowsToRender()) * pSettings.getFieldHeight());
        }
        g2d.drawRenderedImage(img, trans);
        if (isFullRenderRequired()) {
//everything was rendered, skip col rendering
            setFullRenderRequired(false);
        } else {
            img = renderTroopColumns(pSettings);
            trans = AffineTransform.getTranslateInstance(0, 0);
            if (pSettings.getColumnsToRender() < 0) {
                trans.setToTranslation((pSettings.getVisibleVillages().length + pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), 0);
            }
            g2d.drawRenderedImage(img, trans);
        }

        g2d.dispose();
        trans.setToTranslation((int) Math.floor(pSettings.getDeltaX()), (int) Math.floor(pSettings.getDeltaY()));
        pG2d.drawRenderedImage(mLayer, trans);
    }

    private void performCopy(RenderSettings pSettings, Graphics2D pG2D) {
        if (mapPos == null) {
            mapPos = new Point((int) Math.floor(pSettings.getMapBounds().getX()), (int) Math.floor(pSettings.getMapBounds().getY()));
        }

        Point newMapPos = new Point((int) Math.floor(pSettings.getMapBounds().getX()), (int) Math.floor(pSettings.getMapBounds().getY()));

        int fieldsX = newMapPos.x - mapPos.x;
        int fieldsY = newMapPos.y - mapPos.y;

//set new map position
        if (newMapPos.distance(mapPos) != 0.0) {
            mapPos.x = newMapPos.x;
            mapPos.y = newMapPos.y;
        }
//  Composite comp = pG2D.getComposite();
        pG2D.setComposite(AlphaComposite.Src);
        pG2D.copyArea(0, 0, mLayer.getWidth(), mLayer.getHeight(), -fieldsX * pSettings.getFieldWidth(), -fieldsY * pSettings.getFieldHeight());
//  pG2D.setComposite(comp);
    }

    private BufferedImage renderTroopColumns(RenderSettings pSettings) {
        int dx = 0;//(int) Math.floor(pSettings.getDeltaX());
        int dy = 0;//(int) Math.floor(pSettings.getDeltaY());
        //create new buffer for rendering
        BufferedImage newColumns = ImageUtils.createCompatibleBufferedImage(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pSettings.getVisibleVillages().length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = newColumns.createGraphics();
        ImageUtils.setupGraphics(g2d);
        //iterate through entire row
        int cnt = 0;

        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            for (int y = 0; y < pSettings.getVisibleVillages()[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y;
                int col = x - firstCol;
                renderField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), dx, dy, pSettings.getZoom(), g2d);
            }
        }
        g2d.dispose();
        return newColumns;

    }

    private BufferedImage renderTroopRows(RenderSettings pSettings) {
        //calculate first row that will be rendered
        BufferedImage newRows = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pSettings.getVisibleVillages()[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = newRows.createGraphics();
        ImageUtils.setupGraphics(g2d);
        //iterate through entire rows
        int cnt = 0;
        int dx = 0;//(int) Math.floor(pSettings.getDeltaX());
        int dy = 0;//(int) Math.floor(pSettings.getDeltaY());
        Village lastVillageToDraw = null;
        int lastVillageRow = 0;
        int lastVillageCol = 0;
        Village currentMouseVillage = MapPanel.getSingleton().getVillageAtMousePos();
        for (int x = 0; x < pSettings.getVisibleVillages().length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y - firstRow;
                int col = x;
                if (v != null && currentMouseVillage != null && v.equals(currentMouseVillage)) {
                    lastVillageToDraw = v;
                    lastVillageRow = row;
                    lastVillageCol = col;
                } else {
                    renderField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), dx, dy, pSettings.getZoom(), g2d);
                }
            }
        }
        renderField(lastVillageToDraw, lastVillageRow, lastVillageCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), dx, dy, pSettings.getZoom(), g2d);
        return newRows;
    }

    private void renderField(Village v, int row, int colu, int pFieldWidth, int pFieldHeight, int pDx, int pDy, double pZoom, Graphics2D g2d) {
        VillageTroopsHolder holder = null;
        if (v != null && v.isVisibleOnMap() && (holder = TroopsManager.getSingleton().getTroopsForVillage(v)) != null) {
            int maxDef = 650000;
            try {
                maxDef = Integer.parseInt(GlobalOptions.getProperty("max.density.troops"));
            } catch (Exception e) {
                maxDef = 650000;
            }
            double defIn = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
            double defOwn = holder.getDefValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
            double percOfMax = defIn / maxDef;
            double percFromOthers = (defIn - defOwn) / defIn;
            double half = (double) maxDef / 2.0;
            //limit to 100%
            percOfMax = (percOfMax > 1) ? 1 : percOfMax;

            Color col = null;
            Color cb = g2d.getColor();
            if (defIn <= maxDef && defIn > half) {
                float ratio = (float) (defIn - half) / (float) half;
                Color c1 = Color.YELLOW;
                Color c2 = Color.GREEN;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                col = new Color(red, green, blue);
            } else if (defIn <= half) {
                float ratio = (float) defIn / (float) half;
                Color c1 = Color.RED;
                Color c2 = Color.YELLOW;
                int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
                int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
                int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
                col = new Color(red, green, blue);
            } else {
                col = new Color(0, 255, 0);
            }

            //start drawing
            int wOfMax = (int) Math.rint(percOfMax * pFieldWidth);
            int wFromOthers = (int) Math.rint(percFromOthers * wOfMax);
            int h = (int) Math.rint(pFieldHeight / 5 / pZoom);
            //set h to at least 3
            h = Math.max(h, 3);
            //draw default bar view
            g2d.setColor(Color.WHITE);
            g2d.fillRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, pFieldWidth - 2, h);
            g2d.setColor(col);
            g2d.fillRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, wOfMax - 2, h);
            g2d.setColor(Color.BLUE);
            g2d.fillRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, wFromOthers - 2, h);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, pFieldWidth - 2, h);
            g2d.setColor(cb);
        }
    }

    public void reset() {
        shouldReset = true;
    }
}

class TroopAnimator {

    private int iDiameter = 0;
    private boolean pRise = true;
    private boolean bFinished = false;
    private Village v = null;

    public TroopAnimator(Village pVillage) {
        v = pVillage;
    }

    public Village getVillage() {
        return v;
    }

    public boolean isFinished() {
        return bFinished;
    }

    public void update(int row, int col, int pFieldWidth, int pFieldHeight, int pDx, int pDy, Graphics2D g2d) {
        Village villageAtMousePos = MapPanel.getSingleton().getVillageAtMousePos();
        if (villageAtMousePos != null && villageAtMousePos.equals(v)) {
            pRise = true;
        } else {
            pRise = false;
        }
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
        VillageTroopsHolder holder = null;
        if (v != null && (holder = TroopsManager.getSingleton().getTroopsForVillage(v)) != null) {
            double offValue = holder.getOffValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
            double defArchValue = holder.getDefArcherValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
            double defCavValue = holder.getDefCavalryValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
            double defValue = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);

            double fightValueIn = offValue + defValue + defArchValue + defCavValue;
            int centerX = (int) Math.floor((double) col * pFieldWidth + pFieldWidth / 2.0 - 16 + pDx);
            int centerY = (int) Math.floor((double) row * pFieldHeight + pFieldHeight / 2.0 - 16 + pDy);
            int halfDiameter = (int) Math.floor(iDiameter / 2.0);
            Color before = g2d.getColor();
            Composite cb = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            if (fightValueIn == 0) {
                g2d.setColor(Constants.DS_BACK_LIGHT);
                g2d.fillOval(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter);
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
                    g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, 0, degOff);
                }
                if (percDef > 0) {
                    //draw def arc
                    degDef = (int) Math.rint(360 * percDef);
                    g2d.setColor(Color.GREEN);
                    g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff, degDef);
                }
                if (percDefCav > 0) {
                    //draw def cav arc
                    degDefCav = (int) Math.rint(360 * percDefCav);
                    g2d.setColor(Color.YELLOW);
                    g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff + degDef, degDefCav);
                }
                if (percDefArch > 0) {
                    //draw def cav arc
                    g2d.setColor(Color.ORANGE.darker());
                    g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff + degDef + degDefCav, 360 - (degOff + degDef + degDefCav));
                }

                drawLegend(centerX + 16, centerY + 16, offValue, defValue, defCavValue, defArchValue, g2d);

            }
            g2d.setColor(before);
            g2d.setComposite(cb);
        } else {
            bFinished = true;
        }
    }

    private void drawLegend(int x, int y, double offValue, double defValue, double defCavValue, double defArchValue, Graphics2D g2d) {
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
}
/**
 *
 * @author Torridity
 */
/*public class TroopDensityLayerRenderer extends AbstractDirectLayerRenderer {

private Hashtable<Village, TroopAnimator> animators = new Hashtable<Village, TroopAnimator>();

@Override
public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
if (!pSettings.isLayerVisible()) {
return;
}
if (pSettings.getFieldWidth() < 20) {
//field width is too small, skip troop density drawing
return;
}
renderRows(pSettings, pG2d);
}

private void renderRows(RenderSettings pSettings, Graphics2D pG2D) {
//calculate first row that will be rendered
int firstRow = 0;//(pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());
long s = System.currentTimeMillis();
//iterate through entire rows
int cnt = 0;
int dx = (int) Math.floor(pSettings.getDeltaX());
int dy = (int) Math.floor(pSettings.getDeltaY());
double zoom = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
Village lastVillageToDraw = null;
int lastVillageRow = 0;
int lastVillageCol = 0;
Village currentMouseVillage = MapPanel.getSingleton().getVillageAtMousePos();
for (int x = 0; x < pSettings.getVisibleVillages().length; x++) {
//iterate from first row for 'pRows' times
for (int y = firstRow; y < pSettings.getVisibleVillages()[0].length; y++) {
cnt++;
Village v = pSettings.getVisibleVillages()[x][y];
int row = y - firstRow;
int col = x;
if (v != null && currentMouseVillage != null && v.equals(currentMouseVillage)) {
lastVillageToDraw = v;
lastVillageRow = row;
lastVillageCol = col;
} else {
renderField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), dx, dy, zoom, pG2D);
}
}
}
renderField(lastVillageToDraw, lastVillageRow, lastVillageCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), dx, dy, zoom, pG2D);
System.out.println("DUr: " + (System.currentTimeMillis() - s));
}

private void renderField(Village v, int row, int colu, int pFieldWidth, int pFieldHeight, int pDx, int pDy, double pZoom, Graphics2D g2d) {
VillageTroopsHolder holder = null;
if (v != null && (holder = TroopsManager.getSingleton().getTroopsForVillage(v)) != null) {
int maxDef = 650000;
try {
maxDef = Integer.parseInt(GlobalOptions.getProperty("max.density.troops"));
} catch (Exception e) {
maxDef = 650000;
}
double defIn = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
double defOwn = holder.getDefValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
double percOfMax = defIn / maxDef;
double percFromOthers = (defIn - defOwn) / defIn;
double half = (double) maxDef / 2.0;
//limit to 100%
percOfMax = (percOfMax > 1) ? 1 : percOfMax;

Color col = null;
Color cb = g2d.getColor();
if (defIn <= maxDef && defIn > half) {
float ratio = (float) (defIn - half) / (float) half;
Color c1 = Color.YELLOW;
Color c2 = Color.GREEN;
int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
col = new Color(red, green, blue);
} else if (defIn <= half) {
float ratio = (float) defIn / (float) half;
Color c1 = Color.RED;
Color c2 = Color.YELLOW;
int red = (int) (c2.getRed() * ratio + c1.getRed() * (1 - ratio));
int green = (int) (c2.getGreen() * ratio + c1.getGreen() * (1 - ratio));
int blue = (int) (c2.getBlue() * ratio + c1.getBlue() * (1 - ratio));
col = new Color(red, green, blue);
} else {
col = new Color(0, 255, 0);
}
//start drawing

TroopAnimator animator = animators.get(v);
if (animator != null) {
//update existing animator for village
if (animator.isFinished()) {
animators.remove(animator.getVillage());
} else {
animator.update(row, colu, pFieldWidth, pFieldHeight, pDx, pDy, g2d);
}
} else {
Village current = MapPanel.getSingleton().getVillageAtMousePos();
if (current != null && current.equals(v)) {
animator = new TroopAnimator(v);
animators.put(v, animator);
animator.update(row, colu, pFieldWidth, pFieldHeight, pDx, pDy, g2d);
} else {
int wOfMax = (int) Math.rint(percOfMax * pFieldWidth);
int wFromOthers = (int) Math.rint(percFromOthers * wOfMax);
int h = (int) Math.rint(pFieldHeight / 5 / pZoom);
//set h to at least 3
h = Math.max(h, 3);
//draw default bar view
g2d.setColor(Color.WHITE);
g2d.fillRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, pFieldWidth - 2, h);
g2d.setColor(col);
g2d.fillRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, wOfMax - 2, h);
g2d.setColor(Color.BLUE);
g2d.fillRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, wFromOthers - 2, h);
g2d.setColor(Color.BLACK);
g2d.drawRect(colu * pFieldWidth + 1 + pDx, row * pFieldHeight + pFieldHeight - h + pDy, pFieldWidth - 2, h);
}
g2d.setColor(cb);
}
}
}
private boolean shouldReset = false;

public void reset() {
shouldReset = true;
}
}

class TroopAnimator {

private int iDiameter = 0;
private boolean pRise = true;
private boolean bFinished = false;
private Village v = null;

public TroopAnimator(Village pVillage) {
v = pVillage;
}

public Village getVillage() {
return v;
}

public boolean isFinished() {
return bFinished;
}

public void update(int row, int col, int pFieldWidth, int pFieldHeight, int pDx, int pDy, Graphics2D g2d) {
Village villageAtMousePos = MapPanel.getSingleton().getVillageAtMousePos();
if (villageAtMousePos != null && villageAtMousePos.equals(v)) {
pRise = true;
} else {
pRise = false;
}
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
VillageTroopsHolder holder = null;
if (v != null && (holder = TroopsManager.getSingleton().getTroopsForVillage(v)) != null) {
double offValue = holder.getOffValue(TroopsManagerTableModel.SHOW_OWN_TROOPS);
double defArchValue = holder.getDefArcherValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
double defCavValue = holder.getDefCavalryValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
double defValue = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);

double fightValueIn = offValue + defValue + defArchValue + defCavValue;
int centerX = (int) Math.floor((double) col * pFieldWidth + pFieldWidth / 2.0 - 16 + pDx);
int centerY = (int) Math.floor((double) row * pFieldHeight + pFieldHeight / 2.0 - 16 + pDy);
int halfDiameter = (int) Math.floor(iDiameter / 2.0);
Color before = g2d.getColor();
Composite cb = g2d.getComposite();
g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
if (fightValueIn == 0) {
g2d.setColor(Constants.DS_BACK_LIGHT);
g2d.fillOval(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter);
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
g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, 0, degOff);
//drawLegend(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, 0, degOff, offValue, OFF, g2d);
}
if (percDef > 0) {
//draw def arc
degDef = (int) Math.rint(360 * percDef);
g2d.setColor(Color.GREEN);
g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff, degDef);
// drawLegend(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff, degDef, defValue, DEF, g2d);
}
if (percDefCav > 0) {
//draw def cav arc
degDefCav = (int) Math.rint(360 * percDefCav);
g2d.setColor(Color.YELLOW);
g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff + degDef, degDefCav);
// drawLegend(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff + degDef, degDefCav, defCavValue, DEF_CAV, g2d);
}
if (percDefArch > 0) {
//draw def cav arc
g2d.setColor(Color.ORANGE.darker());
g2d.fillArc(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff + degDef + degDefCav, 360 - (degOff + degDef + degDefCav));
// drawLegend(centerX - halfDiameter + 16, centerY - halfDiameter + 16, iDiameter, iDiameter, degOff + degDef + degDefCav, 360 - (degOff + degDef + degDefCav), defArchValue, DEF_ARCH, g2d);
}

drawLegend(centerX + 16, centerY + 16, offValue, defValue, defCavValue, defArchValue, g2d);

}
g2d.setColor(before);
g2d.setComposite(cb);
} else {
bFinished = true;
}
}

private void drawLegend(int x, int y, double offValue, double defValue, double defCavValue, double defArchValue, Graphics2D g2d) {
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
g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_ATTACK), x + 80 + 2, y - 80 + 2, null);
g2d.drawString(offLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + textHeight);
g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_ALLY), x + 80 + 2, y - 80 + 2 + 16 + 2, null);
g2d.drawString(defLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + 16 + 2 + textHeight);
g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_DEF_CAV), x + 80 + 2, y - 80 + 2 + 16 + 2 + 16 + 2, null);
g2d.drawString(defCavLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + 16 + 2 + 16 + 2 + textHeight);
g2d.drawImage(ImageManager.getNoteSymbol(ImageManager.NOTE_SYMBOL_DEF_ARCH), x + 80 + 2, y - 80 + 2 + 16 + 2 + 16 + 2 + 16 + 2, null);
g2d.drawString(defArchLegendValue, x + 80 + 2 + 16 + 2, y - 80 + 2 + 16 + 2 + 16 + 2 + 16 + 2 + textHeight);

g2d.setFont(f);
}
}
 */
