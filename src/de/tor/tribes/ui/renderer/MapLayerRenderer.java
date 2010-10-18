/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;

/**
 *
 * @author Torridity
 */
public class MapLayerRenderer extends AbstractBufferedLayerRenderer {

    private BufferedImage mLayer = null;
    private HashMap<Integer, Rectangle> renderedSpriteBounds = null;
    private HashMap<Integer, Rectangle> renderedMarkerBounds = null;
    private Point mapPos = null;

    @Override
    public void performRendering(Rectangle2D pVirtualBounds, Village[][] pVisibleVillages, Graphics2D pG2d) {
        RenderSettings settings = getRenderSettings(pVirtualBounds);
        Graphics2D g2d = null;
        if (isFullRenderRequired()) {
            if (mLayer == null) {
                mLayer = ImageUtils.createCompatibleBufferedImage(pVisibleVillages.length * settings.getFieldWidth(), pVisibleVillages[0].length * settings.getFieldHeight(), BufferedImage.OPAQUE);
            }
            g2d = (Graphics2D) mLayer.getGraphics();
            g2d.setClip(0, 0, mLayer.getWidth(), mLayer.getHeight());
            settings.setRowsToRender(pVisibleVillages[0].length);
        } else {
            //copy existing data to new location
            g2d = (Graphics2D) mLayer.getGraphics();
            g2d.setClip(0, 0, mLayer.getWidth(), mLayer.getHeight());
            performCopy(settings, pVirtualBounds, g2d);
        }
        ImageUtils.setupGraphics(g2d);
        renderedSpriteBounds = new HashMap<Integer, Rectangle>();
        renderedMarkerBounds = new HashMap<Integer, Rectangle>();

        //Set new bounds
        setRenderedBounds((Rectangle2D.Double) pVirtualBounds.clone());
        //BufferedImage img = renderMarkerRows(pVisibleVillages, settings);
        BufferedImage img = renderMarkerRows(pVisibleVillages, settings);
        Graphics2D ig2d = (Graphics2D) img.getGraphics();
        ig2d.setClip(0, 0, img.getWidth(), img.getHeight());
        ImageUtils.setupGraphics(ig2d);
        //   ig2d.setCompofsite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        //System.err.println("RENDER0");
        ig2d.drawRenderedImage(renderVillageRows(pVisibleVillages, settings), AffineTransform.getTranslateInstance(0, 0));
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        if (settings.getRowsToRender() < 0) {
            trans.setToTranslation(0, (pVisibleVillages[0].length + settings.getRowsToRender()) * settings.getFieldHeight());
        }
        g2d.drawRenderedImage(img, trans);

        if (isFullRenderRequired()) {
            //everything was rendered, skip col rendering
            setFullRenderRequired(false);
        } else {
            renderedSpriteBounds = new HashMap<Integer, Rectangle>();
            renderedMarkerBounds = new HashMap<Integer, Rectangle>();
            img = renderMarkerColumns(pVisibleVillages, settings);
            ig2d = (Graphics2D) img.getGraphics();
            ImageUtils.setupGraphics(ig2d);
            ig2d.drawRenderedImage(renderVillageColumns(pVisibleVillages, settings), AffineTransform.getTranslateInstance(0, 0));
            trans = AffineTransform.getTranslateInstance(0, 0);
            if (settings.getColumnsToRender() < 0) {
                trans.setToTranslation((pVisibleVillages.length + settings.getColumnsToRender()) * settings.getFieldWidth(), 0);
            }
            g2d.drawRenderedImage(img, trans);
        }
        g2d.dispose();
        trans.setToTranslation((int) Math.floor(settings.getDeltaX()), (int) Math.floor(settings.getDeltaY()));
        pG2d.drawRenderedImage(mLayer, trans);
        drawContinents(pVisibleVillages, settings, pG2d);
    }

    private void performCopy(RenderSettings pSettings, Rectangle2D pVirtualBounds, Graphics2D pG2D) {
        if (mapPos == null) {
            mapPos = new Point((int) Math.floor(pVirtualBounds.getX()), (int) Math.floor(pVirtualBounds.getY()));
        }

        Point newMapPos = new Point((int) Math.floor(pVirtualBounds.getX()), (int) Math.floor(pVirtualBounds.getY()));

        int fieldsX = newMapPos.x - mapPos.x;
        int fieldsY = newMapPos.y - mapPos.y;

        //set new map position
        if (newMapPos.distance(mapPos) != 0.0) {
            mapPos.x = newMapPos.x;
            mapPos.y = newMapPos.y;
        }
        pG2D.copyArea(0, 0, mLayer.getWidth(), mLayer.getHeight(), -fieldsX * pSettings.getFieldWidth(), -fieldsY * pSettings.getFieldHeight());
    }

    private void drawContinents(Village[][] pVisibleVillages, RenderSettings pSettings, Graphics2D pG2d) {
        boolean showSectors = false;
        try {
            showSectors = Boolean.parseBoolean(GlobalOptions.getProperty("show.sectors"));
        } catch (Exception e) {
            showSectors = false;
        }

        boolean showContinents = false;
        try {
            showContinents = Boolean.parseBoolean(GlobalOptions.getProperty("map.showcontinents"));
        } catch (Exception e) {
            showContinents = false;
        }

        //draw continents and sectors
        if (mapPos == null) {
            return;
        }
        int contSpacing = 100;
        if (ServerSettings.getSingleton().getCoordType() != 2) {
            contSpacing = 50;
        }

        int fieldHeight = GlobalOptions.getSkin().getCurrentFieldHeight();
        int fieldWidth = GlobalOptions.getSkin().getCurrentFieldWidth();
        //draw vertical borders
        for (int i = mapPos.x; i < mapPos.x + pVisibleVillages.length; i++) {
            if (i % 5 == 0) {
                boolean draw = true;
                if (i % contSpacing == 0) {
                    if (!showSectors) {
                        draw = false;
                    } else {
                        pG2d.setStroke(new BasicStroke(1.0f));
                        pG2d.setColor(Color.YELLOW);
                    }
                } else {
                    if (!showContinents) {
                        draw = false;
                    } else {
                        pG2d.setStroke(new BasicStroke(0.5f));
                        pG2d.setColor(Color.BLACK);
                    }
                }
                if (draw) {
                    pG2d.draw(new Line2D.Double((i - mapPos.x) * fieldWidth + (int) Math.floor(pSettings.getDeltaX()), 0, (i - mapPos.x) * fieldWidth + (int) Math.floor(pSettings.getDeltaX()), pVisibleVillages[0].length * fieldHeight));
                }
            }
        }
        //draw horizontal borders
        for (int i = mapPos.y; i < mapPos.y + pVisibleVillages[0].length; i++) {
            if (i % 5 == 0) {
                boolean draw = true;
                if (i % contSpacing == 0) {
                    if (!showSectors) {
                        draw = false;
                    } else {
                        pG2d.setStroke(new BasicStroke(1.0f));
                        pG2d.setColor(Color.YELLOW);
                    }
                } else {
                    if (!showContinents) {
                        draw = false;
                    } else {
                        pG2d.setStroke(new BasicStroke(0.5f));
                        pG2d.setColor(Color.BLACK);
                    }
                }
                if (draw) {
                    pG2d.draw(new Line2D.Double(0, (i - mapPos.y) * fieldHeight + (int) Math.floor(pSettings.getDeltaY()), pVisibleVillages.length * fieldWidth, (i - mapPos.y) * fieldHeight + (int) Math.floor(pSettings.getDeltaY())));
                }
            }
        }
    }

    private BufferedImage renderVillageRows(Village[][] pVillages, RenderSettings pSettings) {
        //create new buffer for rendering
        //BufferedImage newRows = createEmptyBuffered(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.TRANSLUCENT);
        BufferedImage newRows = ImageUtils.createCompatibleBufferedImage(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        ImageUtils.setupGraphics(g2d);
        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }

        for (int x = 0; x < pVillages.length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;
                int globalCol = colToGlobalPosition(col);
                int globalRow = rowToGlobalPosition(y);
                renderVillageField(v, row, col, globalRow, globalCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        g2d.dispose();
        return newRows;
    }

    private BufferedImage renderMarkerRows(Village[][] pVillages, RenderSettings pSettings) {
        //create new buffer for rendering
        //BufferedImage newRows = createEmptyBuffered(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.TRANSLUCENT);
        BufferedImage newRows = ImageUtils.createCompatibleBufferedImage(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.OPAQUE);
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        ImageUtils.setupGraphics(g2d);
        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }

        for (int x = 0; x < pVillages.length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;
                renderMarkerField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        g2d.dispose();
        return newRows;
    }

    private BufferedImage renderVillageColumns(Village[][] pVillages, RenderSettings pSettings) {
        //create new buffer for rendering
        //  BufferedImage newColumns = createEmptyBuffered(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.TRANSLUCENT);
        BufferedImage newColumns = ImageUtils.createCompatibleBufferedImage(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pVillages.length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        ImageUtils.setupGraphics(g2d);

        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            for (int y = 0; y < pVillages[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pVillages[x][y];
                int row = y;
                int col = x - firstCol;
                int globalCol = colToGlobalPosition(x);
                int globalRow = rowToGlobalPosition(row);
                renderVillageField(v, row, col, globalRow, globalCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        g2d.dispose();
        return newColumns;
    }

    private BufferedImage renderMarkerColumns(Village[][] pVillages, RenderSettings pSettings) {
        //create new buffer for rendering
        //BufferedImage newColumns = createEmptyBuffered(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.TRANSLUCENT);
        BufferedImage newColumns = ImageUtils.createCompatibleBufferedImage(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.OPAQUE);
        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pVillages.length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        ImageUtils.setupGraphics(g2d);

        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            for (int y = 0; y < pVillages[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pVillages[x][y];
                int row = y;
                int col = x - firstCol;
                renderMarkerField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        g2d.dispose();
        return newColumns;
    }

    
    private void renderVillageField(Village v, int row, int col, int globalRow, int globalCol, int pFieldWidth, int pFieldHeight, double zoom, boolean useDecoration, Graphics2D g2d) {
        Rectangle copyRect = null;
        int textureId = -1;
        BufferedImage sprite = null;
        boolean showBarbarian = true;
        try {
            showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
        } catch (Exception e) {
            showBarbarian = true;
        }

        boolean markedOnly = false;
        try {
            markedOnly = Boolean.parseBoolean(GlobalOptions.getProperty("draw.marked.only"));
        } catch (Exception e) {
            markedOnly = false;
        }

        if (v != null
                && !(v.getTribe().equals(Barbarians.getSingleton()) && !showBarbarian)
                && !(MarkerManager.getSingleton().getMarker(v) == null && markedOnly)) {
            //village field that has to be rendered
            if (GlobalOptions.getSkin().isMinimapSkin()) {
                textureId = Skin.ID_V1;
            } else {
                textureId = v.getGraphicsType();
            }
            copyRect = renderedSpriteBounds.get(textureId);
            if (copyRect == null) {
                sprite = GlobalOptions.getSkin().getOriginalSprite(textureId);
            }
        } else {
            if (useDecoration) {
                textureId = WorldDecorationHolder.getTextureId(globalCol, globalRow) + 100;
            } else {
                textureId = Skin.ID_DEFAULT_UNDERGROUND + 100;
            }
            copyRect = renderedSpriteBounds.get(textureId);
            if (copyRect == null && useDecoration) {
                sprite = WorldDecorationHolder.getOriginalSprite(globalCol, globalRow);
            } else if (copyRect == null && !useDecoration) {
                sprite = GlobalOptions.getSkin().getOriginalSprite(Skin.ID_DEFAULT_UNDERGROUND);
            }
        }

        //render sprite or copy area if sprite is null
        if (sprite != null) {
            //render sprite
            AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
            t.scale(1 / zoom, 1 / zoom);
            // System.err.println("RENDERX");
            g2d.drawRenderedImage(sprite, t);
            //g2d.drawImage(sprite, col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight, null, null);//mage(sprite, t, null);
            renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight));
        } else if (copyRect != null) {
            //copy from copy rect
            g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x, row * pFieldHeight - copyRect.y);
        }
    }


    private void renderMarkerField(Village v, int row, int col, int pFieldWidth, int pFieldHeight, double zoom, boolean useDecoration, Graphics2D g2d) {
        int tribeId = -666;
        BufferedImage sprite = null;
        Rectangle copyRect = null;
        boolean showBarbarian = true;
        try {
            showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
        } catch (Exception e) {
            showBarbarian = true;
        }
        boolean markedOnly = false;
        try {
            markedOnly = Boolean.parseBoolean(GlobalOptions.getProperty("draw.marked.only"));
        } catch (Exception e) {
            markedOnly = false;
        }
        if (v != null
                && !(v.getTribe().equals(Barbarians.getSingleton()) && !showBarbarian)
                && !(MarkerManager.getSingleton().getMarker(v) == null && markedOnly)) {
            tribeId = v.getTribeID();
            copyRect = renderedMarkerBounds.get(tribeId);
            if (copyRect == null) {
                sprite = getMarker(v);
            }
        } else {
            return;
        }

        //render sprite or copy area if sprite is null
        if (sprite != null) {
            //render sprite
            AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
            t.scale(1.0 / zoom, 1.0 / zoom);
            //  System.err.println("RENDER");
            g2d.drawRenderedImage(sprite, t);
            //g2d.drawRenderedImage(sprite, t);
            renderedMarkerBounds.put(tribeId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight));
        } else if (copyRect != null) {
            // System.err.println("COPY");
            g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x, row * pFieldHeight - copyRect.y);
        }
    }

    private int rowToGlobalPosition(int pRow) {
        int yPos = (int) Math.floor(getRenderedBounds().getY());
        return yPos + pRow;
    }

    private int colToGlobalPosition(int pCol) {
        int xPos = (int) Math.floor(getRenderedBounds().getX());
        return xPos + pCol;
    }

    private BufferedImage getMarker(Village pVillage) {
        int w = GlobalOptions.getSkin().getBasicFieldWidth();
        int h = GlobalOptions.getSkin().getBasicFieldHeight();
        BufferedImage image = ImageUtils.createCompatibleBufferedImage(w, h, BufferedImage.OPAQUE);
        Color markerColor = null;
        Marker tribeMarker = null;
        Marker allyMarker = null;
        Color DEFAULT = null;
        try {
            int mark = Integer.parseInt(GlobalOptions.getProperty("default.mark"));
            if (mark == 0) {
                DEFAULT = Constants.DS_DEFAULT_MARKER;
            } else if (mark == 1) {
                DEFAULT = Color.RED;
            } else if (mark == 2) {
                DEFAULT = Color.WHITE;
            }

        } catch (Exception e) {
            DEFAULT = Constants.DS_DEFAULT_MARKER;
        }
        Village currentUserVillage = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
        if (currentUserVillage != null && pVillage.getTribe() == currentUserVillage.getTribe()) {
            if (pVillage.equals(currentUserVillage)) {
                markerColor = Color.WHITE;
            } else {
                markerColor = Color.YELLOW;
            }
        } else {
            if (pVillage.getTribe() != Barbarians.getSingleton()) {
                tribeMarker = MarkerManager.getSingleton().getMarker(pVillage.getTribe());
                if (pVillage.getTribe().getAlly() != BarbarianAlly.getSingleton()) {
                    allyMarker = MarkerManager.getSingleton().getMarker(pVillage.getTribe().getAlly());
                }
            }
        }
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        ImageUtils.setupGraphics(g2d);
        if (markerColor != null || tribeMarker != null || allyMarker != null) {
            if (tribeMarker != null && allyMarker != null) {
                //draw two-part marker
                GeneralPath p = new GeneralPath();
                p.moveTo(0, 0);
                p.lineTo(w, h);
                p.lineTo(0, h);
                p.closePath();
                g2d.setColor(tribeMarker.getMarkerColor());
                g2d.fill(p);
                p = new GeneralPath();
                p.moveTo(0, 0);
                p.lineTo(w, 0);
                p.lineTo(w, h);
                p.closePath();
                g2d.setColor(allyMarker.getMarkerColor());
                g2d.fill(p);
            } else if (tribeMarker != null) {
                //draw tribe marker
                g2d.setColor(tribeMarker.getMarkerColor());
                Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
                g2d.fill(r);
                //g2d.fillRect(0, 0, w, h);
            } else if (allyMarker != null) {
                //draw ally marker
                g2d.setColor(allyMarker.getMarkerColor());
                Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
                g2d.fill(r);
                // g2d.fillRect(0, 0, w, h);
            } else {
                //draw misc marker
                g2d.setColor(markerColor);
                Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
                g2d.fill(r);
                //g2d.fillRect(0, 0, w, h);
            }
        } else {
            if (pVillage.getTribe() != Barbarians.getSingleton()) {
                if (DEFAULT != null) {
                    //no mark-on-top mode
                    g2d.setColor(DEFAULT);
                    Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
                    g2d.fill(r);
                    //  g2d.fillRect(0, 0, w, h);
                }
            } else {
                //barbarian marker
                g2d.setColor(Color.LIGHT_GRAY);
                Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
                g2d.fill(r);
                //g2d.fillRect(0, 0, w, h);
            }
        }
        g2d.setColor(Color.BLACK);
        Rectangle2D.Double r = new Rectangle2D.Double(0, 0, w, h);
        g2d.draw(r);
        // g2d.drawRect(0, 0, w, h);
        r = new Rectangle2D.Double(1, 1, w - 3, h - 3);
        g2d.draw(r);
        // g2d.drawRect(1, 1, w - 3, h - 3);
        g2d.dispose();
        return image;
    }

    public void reset() {
        setRenderedBounds(null);
        setFullRenderRequired(true);
    }
}
