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
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.Skin;
import de.tor.tribes.util.mark.MarkerManager;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
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
    private boolean bMarkOnTop = false;
    private boolean shouldReset = true;
    private double lastZoom = -666.0;

    public void setMarkOnTop(boolean pValue) {
        bMarkOnTop = pValue;
    }

    public boolean isMarkOnTop() {
        return bMarkOnTop;
    }

    @Override
    public void performRendering(RenderSettings pSettings, Graphics2D pG2d) {
        if (shouldReset) {
            setFullRenderRequired(true);
            shouldReset = false;
            mapPos = null;
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
        Graphics2D g2d = null;

        if (mapPos != null && mLayer != null && !isFullRenderRequired()) {
            Point newMapPos = new Point((int) Math.floor(pSettings.getMapBounds().getX()), (int) Math.floor(pSettings.getMapBounds().getY()));
            if (mapPos.distance(newMapPos) != 0) {
                moved = true;
            } else {
                moved = false;
            }
        } else {
            moved = true;
        }

        long s = System.currentTimeMillis();
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        if (mapPos == null) {
            mapPos = new Point((int) Math.floor(pSettings.getMapBounds().getX()), (int) Math.floor(pSettings.getMapBounds().getY()));
        }
        BufferedImage img = null;
        if (moved || isFullRenderRequired()) {
            if (isFullRenderRequired()) {
                if (mLayer == null) {
                    try {
                        mLayer = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), Transparency.OPAQUE);
                    } catch (Exception e) {
                        mLayer = null;
                        return;
                    }
                }
                g2d = (Graphics2D) mLayer.getGraphics();
                // g2d.fillRect(0, 0, mLayer.getWidth(), mLayer.getHeight());
                //g2d.setClip(0, 0, mLayer.getWidth(), mLayer.getHeight());
                pSettings.setRowsToRender(pSettings.getVisibleVillages()[0].length);
                mapPos = new Point((int) Math.floor(pSettings.getMapBounds().getX()), (int) Math.floor(pSettings.getMapBounds().getY()));
            } else {
                //copy existing data to new location
                g2d = (Graphics2D) mLayer.getGraphics();
                // g2d.fillRect(0, 0, mLayer.getWidth(), mLayer.getHeight());
                performCopy(pSettings, g2d);
            }
            ImageUtils.setupGraphics(g2d);

            renderedSpriteBounds = new HashMap<Integer, Rectangle>();
            renderedMarkerBounds = new HashMap<Integer, Rectangle>();


            if (isMarkOnTop()) {
                img = renderVillageRows(pSettings);
            } else {
                img = renderMarkerRows(pSettings);
            }
            Graphics2D ig2d = (Graphics2D) img.getGraphics();
            // ig2d.setClip(0, 0, img.getWidth(), img.getHeight());
            ImageUtils.setupGraphics(ig2d);
            if (isMarkOnTop()) {
                Composite c = ig2d.getComposite();
                ig2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
                ig2d.drawImage(renderMarkerRows(pSettings), 0, 0, null);
                ig2d.setComposite(c);
            } else {
                ig2d.drawImage(renderVillageRows(pSettings), 0, 0, null);
            }

            if (pSettings.getRowsToRender() < 0) {
                trans.setToTranslation(0, (pSettings.getVisibleVillages()[0].length + pSettings.getRowsToRender()) * pSettings.getFieldHeight());
            }
            g2d.drawRenderedImage(img, trans);//Image(img, (int) Math.floor(trans.getTranslateX()), (int) Math.floor(trans.getTranslateY()), null);
            img.flush();

            if (isFullRenderRequired()) {
                //everything was rendered, skip col rendering
                setFullRenderRequired(false);
            } else {
                renderedSpriteBounds = new HashMap<Integer, Rectangle>();
                renderedMarkerBounds = new HashMap<Integer, Rectangle>();
                if (isMarkOnTop()) {
                    img = renderVillageColumns(pSettings);
                } else {
                    img = renderMarkerColumns(pSettings);
                }
                ig2d = (Graphics2D) img.getGraphics();
                ImageUtils.setupGraphics(ig2d);
                if (isMarkOnTop()) {
                    Composite c = ig2d.getComposite();
                    ig2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
                    ig2d.drawImage(renderMarkerColumns(pSettings), 0, 0, null);
                    ig2d.setComposite(c);
                } else {
                    ig2d.drawImage(renderVillageColumns(pSettings), 0, 0, null);
                }
                trans = AffineTransform.getTranslateInstance(0, 0);
                if (pSettings.getColumnsToRender() < 0) {
                    trans.setToTranslation((pSettings.getVisibleVillages().length + pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), 0);
                }
                g2d.drawRenderedImage(img, trans);//Image(img, (int) Math.floor(trans.getTranslateX()), (int) Math.floor(trans.getTranslateY()), null);
            }

            ig2d.dispose();
            g2d.dispose();
            // img.flush();
        }
        /*  System.out.println("Delta: " + pSettings.getDeltaX() + "/" + pSettings.getDeltaY());
        System.out.println("Cols: " + pSettings.getColumnsToRender());
        System.out.println("FieldS: " + pSettings.getFieldWidth());
        System.out.println("----------");*/

        trans = AffineTransform.getTranslateInstance(pSettings.getDeltaX(), pSettings.getDeltaY());
        pG2d.drawRenderedImage(mLayer, trans);//AffineTransform.getTranslateInstance(0,0));

        drawContinents(pSettings, pG2d);
        pSettings.setLayerVisible(true);

    }
    boolean moved = true;

    public boolean hasMoved() {
        return moved;
    }

    private void performCopy(RenderSettings pSettings, Graphics2D pG2D) {

        /*   System.out.println("MPH " + MapPanel.getSingleton().getHeight());
        System.out.println("LH  " + mLayer.getHeight());
        System.out.println("VY: " + pSettings.getVisibleVillages()[0].length);
        System.out.println("MY: " + pSettings.getRowsToRender());
        System.out.println("FH: " + pSettings.getFieldHeight());*/

        Point newMapPos = new Point((int) Math.floor(pSettings.getMapBounds().getX()), (int) Math.floor(pSettings.getMapBounds().getY()));

        int fieldsX = newMapPos.x - mapPos.x;
        int fieldsY = newMapPos.y - mapPos.y;
        mapPos = (Point) newMapPos.clone();
        //set new map position
        //  System.out.println("Move " + (-fieldsX * pSettings.getFieldWidth()) + "/" + (-fieldsY * pSettings.getFieldHeight()));
        //    System.out.println("Move: " + (-fieldsY * pSettings.getFieldHeight()));
        pG2D.copyArea(0, 0, mLayer.getWidth(), mLayer.getHeight(), -fieldsX * pSettings.getFieldWidth(), -fieldsY * pSettings.getFieldHeight());
    }

    private void drawContinents(RenderSettings pSettings, Graphics2D pG2d) {
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
        int fieldHeight = pSettings.getFieldHeight();
        int fieldWidth = pSettings.getFieldWidth();
        //draw vertical borders
        for (int i = mapPos.x; i < mapPos.x + pSettings.getVisibleVillages().length + 1; i++) {
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
                    pG2d.drawLine((i - mapPos.x) * fieldWidth + (int) Math.floor(pSettings.getDeltaX()), 0, (i - mapPos.x) * fieldWidth + (int) Math.floor(pSettings.getDeltaX()), pSettings.getVisibleVillages()[0].length * (fieldHeight + 1));
                }
            }
        }
        //draw horizontal borders
        for (int i = mapPos.y; i < mapPos.y + pSettings.getVisibleVillages()[0].length + 1; i++) {
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
                    pG2d.drawLine(0, (i - mapPos.y) * fieldHeight + (int) Math.floor(pSettings.getDeltaY()), (pSettings.getVisibleVillages().length + 1) * fieldWidth, (i - mapPos.y) * fieldHeight + (int) Math.floor(pSettings.getDeltaY()));
                }
            }
        }
    }

    private BufferedImage renderVillageRows(RenderSettings pSettings) {
        //create new buffer for rendering
        //BufferedImage newRows = createEmptyBuffered(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.TRANSLUCENT);
        BufferedImage newRows = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), Transparency.BITMASK);
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pSettings.getVisibleVillages()[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        ImageUtils.setupGraphics(g2d);
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
        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if (!WorldDecorationHolder.isValid()) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        for (int x = 0; x < pSettings.getVisibleVillages().length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y - firstRow;
                int col = x;
                int globalCol = colToGlobalPosition(pSettings, col);
                int globalRow = rowToGlobalPosition(pSettings, y);
                renderVillageField(v, row, col, globalRow, globalCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, showBarbarian, markedOnly, g2d);
            }
        }
        g2d.dispose();
        return newRows;
    }

    private BufferedImage renderMarkerRows(RenderSettings pSettings) {
        //create new buffer for rendering
        BufferedImage newRows = null;
        if (isMarkOnTop()) {
            newRows = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), Transparency.TRANSLUCENT);
        } else {
            newRows = ImageUtils.createCompatibleBufferedImage(pSettings.getVisibleVillages().length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), Transparency.OPAQUE);
        }
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pSettings.getVisibleVillages()[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        ImageUtils.setupGraphics(g2d);
        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if (!WorldDecorationHolder.isValid()) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }

        for (int x = 0; x < pSettings.getVisibleVillages().length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y - firstRow;
                int col = x;
                renderMarkerField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        g2d.dispose();
        return newRows;
    }

    private BufferedImage renderVillageColumns(RenderSettings pSettings) {
        //create new buffer for rendering
        //  BufferedImage newColumns = createEmptyBuffered(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.TRANSLUCENT);
        BufferedImage newColumns = ImageUtils.createCompatibleBufferedImage(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), Transparency.BITMASK);

        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pSettings.getVisibleVillages().length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        ImageUtils.setupGraphics(g2d);
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
        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if (!WorldDecorationHolder.isValid()) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        /*     System.out.println("===============");
        System.out.println("L " + pSettings.getVisibleVillages().length);
         */
        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            /*    System.out.println("Render col " + x);
            System.out.println("FW " + pSettings.getFieldWidth());
            System.out.println("GC: " + colToGlobalPosition(pSettings, x));
            System.out.println("LA " + mLayer.getWidth());
            boolean pr = false;*/
            for (int y = 0; y < pSettings.getVisibleVillages()[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y;
                int col = x - firstCol;
                int globalCol = colToGlobalPosition(pSettings, x);
                int globalRow = rowToGlobalPosition(pSettings, row);
                /*       if (!pr) {
                System.out.println("VIL " + row + "/" + col + "-" + globalRow + "/" + globalCol);
                pr = true;
                }*/
                renderVillageField(v, row, col, globalRow, globalCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, showBarbarian, markedOnly, g2d);
            }
        }
        g2d.dispose();
        //       System.out.println("===============");
        return newColumns;
    }

    private BufferedImage renderMarkerColumns(RenderSettings pSettings) {
        //create new buffer for rendering
        BufferedImage newColumns = null;
        if (isMarkOnTop()) {
            newColumns = ImageUtils.createCompatibleBufferedImage(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), Transparency.TRANSLUCENT);
        } else {
            newColumns = ImageUtils.createCompatibleBufferedImage(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pSettings.getVisibleVillages()[0].length * pSettings.getFieldHeight(), Transparency.OPAQUE);
        }

        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pSettings.getVisibleVillages().length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        ImageUtils.setupGraphics(g2d);

        //iterate through entire row
        int cnt = 0;
        boolean useDecoration = true;

        if (!WorldDecorationHolder.isValid()) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }

        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            for (int y = 0; y < pSettings.getVisibleVillages()[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pSettings.getVisibleVillages()[x][y];
                int row = y;
                int col = x - firstCol;
                renderMarkerField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        g2d.dispose();
        return newColumns;
    }

    private void renderVillageField(Village v,
            int row,
            int col,
            int globalRow,
            int globalCol,
            int pFieldWidth,
            int pFieldHeight,
            double zoom,
            boolean useDecoration,
            boolean showBarbarian,
            boolean markedOnly,
            Graphics2D g2d) {
        Rectangle copyRect = null;
        int textureId = -1;
        BufferedImage sprite = null;

        if (v != null
                && !(v.getTribe().equals(Barbarians.getSingleton()) && !showBarbarian)
                && !(MarkerManager.getSingleton().getMarker(v) == null && markedOnly)) {
            //village field that has to be rendered
            v.setVisibleOnMap(true);
            if (GlobalOptions.getSkin().isMinimapSkin()) {
                textureId = Skin.ID_V1;
            } else {
                textureId = v.getGraphicsType();
            }
            copyRect = renderedSpriteBounds.get(textureId);
            if (copyRect == null) {
                sprite = GlobalOptions.getSkin().getCachedImage(textureId, zoom);
            }
        } else {
            if (v != null) {
                v.setVisibleOnMap(false);
            }
            if (useDecoration) {
                textureId = WorldDecorationHolder.getTextureId(globalCol, globalRow) + 100;
            } else {
                textureId = Skin.ID_DEFAULT_UNDERGROUND;
            }
            copyRect = renderedSpriteBounds.get(textureId);
            if (copyRect == null && useDecoration) {
                //sprite = WorldDecorationHolder.getOriginalSprite(globalCol, globalRow);
                sprite = WorldDecorationHolder.getCachedImage(globalCol, globalRow, zoom);
            } else if (copyRect == null && !useDecoration) {
                sprite = GlobalOptions.getSkin().getCachedImage(textureId, zoom);
            }
        }

        //render sprite or copy area if sprite is null
        int posX = col * pFieldWidth;
        int posY = row * pFieldHeight;
        if (sprite != null) {
            //render sprite
            if (isMarkOnTop()) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(posX, posY, pFieldWidth, pFieldHeight);
            }
            g2d.drawImage(sprite, posX, posY, null);
            renderedSpriteBounds.put(textureId, new Rectangle(posX, posY, pFieldWidth, pFieldHeight));
        } else if (copyRect != null) {
            //copy from copy rect
            g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, posX - copyRect.x, posY - copyRect.y);
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
            v.setVisibleOnMap(true);
            tribeId = v.getTribeID();
            copyRect = renderedMarkerBounds.get(tribeId);
            if (copyRect == null) {
                sprite = getMarker(v);
            }
        } else {
            if (v != null) {
                v.setVisibleOnMap(false);
            }
            return;
        }

        //render sprite or copy area if sprite is null
        if (sprite != null) {
            //render sprite
            AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
            t.scale(1.0 / zoom, 1.0 / zoom);
            g2d.drawRenderedImage(sprite, t);
            renderedMarkerBounds.put(tribeId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight));
            sprite.flush();
        } else if (copyRect != null) {
            g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x, row * pFieldHeight - copyRect.y);
        }
    }

    private int rowToGlobalPosition(RenderSettings pSettings, int pRow) {
        int yPos = (int) Math.floor(pSettings.getMapBounds().getY());
        return yPos + pRow;
    }

    private int colToGlobalPosition(RenderSettings pSettings, int pCol) {
        int xPos = (int) Math.floor(pSettings.getMapBounds().getX());
        return xPos + pCol;
    }

    private BufferedImage getMarker(Village pVillage) {
        int w = GlobalOptions.getSkin().getBasicFieldWidth();
        int h = GlobalOptions.getSkin().getBasicFieldHeight();
        BufferedImage image = ImageUtils.createCompatibleBufferedImage(w, h, Transparency.OPAQUE);
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
        Graphics2D g2d = image.createGraphics();
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
                g2d.fillRect(0, 0, w, h);
            } else if (allyMarker != null) {
                //draw ally marker
                g2d.setColor(allyMarker.getMarkerColor());
                g2d.fillRect(0, 0, w, h);
            } else {
                //draw misc marker
                g2d.setColor(markerColor);
                g2d.fillRect(0, 0, w, h);
            }
        } else {
            if (pVillage.getTribe() != Barbarians.getSingleton()) {
                if (DEFAULT != null) {
                    //no mark-on-top mode
                    g2d.setColor(DEFAULT);
                    g2d.fillRect(0, 0, w, h);
                }
            } else {
                //barbarian marker
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, w, h);
            }
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, w, h);
        g2d.dispose();
        return image;
    }

    public void reset() {
        shouldReset = true;
    }
}
