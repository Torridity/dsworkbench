/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.tag.TagManager;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Torridity
 */
public class TagMarkerLayerRenderer extends AbstractBufferedLayerRenderer {

    private BufferedImage mLayer = null;
    private HashMap<Tag, Rectangle> renderedSpriteBounds = null;
    private Point mapPos = null;
    private BufferedImage mConquerWarning = null;

    public TagMarkerLayerRenderer() {
        super();
        try {
            mConquerWarning = ImageIO.read(new File("./graphics/icons/warning.png"));
        } catch (Exception e) {
        }
    }

    @Override
    public void performRendering(Rectangle2D pVirtualBounds, Village[][] pVisibleVillages, Graphics2D pG2d) {
        RenderSettings settings = getRenderSettings(pVirtualBounds);
        Graphics2D g2d = null;
        if (isFullRenderRequired()) {
            if (mLayer == null) {
                mLayer = createEmptyBuffered(pVisibleVillages.length * settings.getFieldWidth(), pVisibleVillages[0].length * settings.getFieldHeight(), BufferedImage.BITMASK);
                mLayer = optimizeImage(mLayer);
                g2d = (Graphics2D) mLayer.getGraphics();
            } else {
                g2d = (Graphics2D) mLayer.getGraphics();
                Composite c = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f));
                g2d.fillRect(0, 0, mLayer.getWidth(), mLayer.getHeight());
                g2d.setComposite(c);
            }

            settings.setRowsToRender(pVisibleVillages[0].length);
        } else {
            //copy existing data to new location
            g2d = (Graphics2D) mLayer.getGraphics();
            performCopy(settings, pVirtualBounds, g2d);
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        renderedSpriteBounds = new HashMap<Tag, Rectangle>();
        //Set new bounds
        setRenderedBounds((Rectangle2D.Double) pVirtualBounds.clone());

        BufferedImage img = renderMarkerRows(pVisibleVillages, settings);
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        if (settings.getRowsToRender() < 0) {
            trans.setToTranslation(0, (pVisibleVillages[0].length + settings.getRowsToRender()) * settings.getFieldHeight());
        }
        g2d.drawRenderedImage(img, trans);
        if (isFullRenderRequired()) {
            //everything was rendered, skip col rendering
             setFullRenderRequired(false);
        } else {
            renderedSpriteBounds = new HashMap<Tag, Rectangle>();

            img = renderMarkerColumns(pVisibleVillages, settings);
            trans = AffineTransform.getTranslateInstance(0, 0);
            if (settings.getColumnsToRender() < 0) {
                trans.setToTranslation((pVisibleVillages.length + settings.getColumnsToRender()) * settings.getFieldWidth(), 0);
            }
            g2d.drawRenderedImage(img, trans);
        }

        trans.setToTranslation((int) Math.floor(settings.getDeltaX()), (int) Math.floor(settings.getDeltaY()));
        pG2d.drawRenderedImage(mLayer, trans);
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
        pG2D.setComposite(AlphaComposite.Src);
        pG2D.copyArea(0, 0, mLayer.getWidth(), mLayer.getHeight(), -fieldsX * pSettings.getFieldWidth(), -fieldsY * pSettings.getFieldHeight());
    }

    private BufferedImage renderMarkerRows(Village[][] pVillages, RenderSettings pSettings) {
        //create new buffer for rendering
        BufferedImage newRows = createEmptyBuffered(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        //iterate through entire row
        int cnt = 0;

        for (int x = 0; x < pVillages.length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;
                renderMarkerField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), g2d);
            }
        }
        g2d.dispose();
        return newRows;
    }

    private BufferedImage renderMarkerColumns(Village[][] pVillages, RenderSettings pSettings) {
        //create new buffer for rendering
        BufferedImage newColumns = createEmptyBuffered(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pVillages.length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        //iterate through entire row
        int cnt = 0;

        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            for (int y = 0; y < pVillages[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pVillages[x][y];
                int row = y;
                int col = x - firstCol;

                renderMarkerField(v, row, col, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), g2d);
            }
        }
        g2d.dispose();
        return newColumns;
    }

    private void renderMarkerField(Village v, int row, int col, int pFieldWidth, int pFieldHeight, double zoom, Graphics2D g2d) {
        BufferedImage sprite = null;
        if (v != null) {
            int tagsize = (int) Math.rint((double) 18 / zoom);
            int conquerSize = (int) Math.rint((double) 16 / zoom);
            if (tagsize > pFieldHeight || tagsize > pFieldWidth) {
                return;
            }
            //sprite = getMarker(v, conquerSize, tagsize, row, col, zoom, pFieldWidth, pFieldHeight);

            List<Tag> villageTags = TagManager.getSingleton().getTags(v);
            if (villageTags != null && !villageTags.isEmpty()) {
                int xcnt = 1;
                int ycnt = 2;
                int cnt = 0;
                for (Tag tag : TagManager.getSingleton().getTags(v)) {
                    // <editor-fold defaultstate="collapsed" desc="Draw tag if active">
                    if (tag.isShowOnMap()) {
                        Rectangle copyRect = renderedSpriteBounds.get(tag);
                        int tagX = col * pFieldWidth + pFieldWidth - xcnt * tagsize;
                        int tagY = row * pFieldHeight + pFieldHeight - ycnt * tagsize;
                        if (copyRect == null) {
                            int iconType = tag.getTagIcon();
                            Color color = tag.getTagColor();

                            if (color != null || iconType != -1) {
                                if (color != null) {
                                    Color before = g2d.getColor();
                                    g2d.setColor(color);
                                    g2d.fill(new Rectangle2D.Double(tagX, tagY, tagsize, tagsize));
                                    g2d.setColor(before);
                                }

                                if (iconType != -1) {
                                    //drawing
                                    Image tagImage = ImageManager.getUnitImage(iconType, false).getScaledInstance(tagsize, tagsize, Image.SCALE_FAST);
                                    g2d.drawImage(tagImage, tagX, tagY, null);
                                }
                                g2d.dispose();
                                //AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
                                //t.scale(1.0 / zoom, 1.0 / zoom);
                                //g2d.drawImage(tag, t, null);
                                renderedSpriteBounds.put(tag, new Rectangle(tagX, tagY, tagsize, tagsize));
                            }
                        } else {
                            g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, tagX - copyRect.x, tagY - copyRect.y);
                        }
                        //calculate positioning
                        cnt++;
                        xcnt++;
                        if (cnt == 2) {
                            //show only 2 icons in the first line to avoid marker overlay
                            xcnt = 1;
                            ycnt--;

                        }
                    }
                    // </editor-fold>
                }
            }



        }

        /* //render sprite or copy area if sprite is null
        if (sprite != null) {
        //render sprite
        AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
        t.scale(1.0 / zoom, 1.0 / zoom);
        g2d.drawRenderedImage(sprite, t);
        }*/
    }

    private BufferedImage getMarker(Village pVillage, int pConquerSize, int pTagSize, int row, int col, double zoom, int pFieldWidth, int pFieldHeight) {
        int w = GlobalOptions.getSkin().getBasicFieldWidth();
        int h = GlobalOptions.getSkin().getBasicFieldHeight();
        BufferedImage image = createEmptyBuffered(w, h, BufferedImage.BITMASK);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Conquer c = ConquerManager.getSingleton().getConquer(pVillage);
        List<Tag> villageTags = TagManager.getSingleton().getTags(pVillage);
        if (villageTags != null && !villageTags.isEmpty()) {
            int xcnt = 1;
            int ycnt = 2;
            int cnt = 0;
            for (Tag tag : TagManager.getSingleton().getTags(pVillage)) {
                // <editor-fold defaultstate="collapsed" desc="Draw tag if active">
                if (tag.isShowOnMap()) {
                    Rectangle copyRect = renderedSpriteBounds.get(tag);
                    int tagX = w - xcnt * pTagSize;
                    int tagY = h - ycnt * pTagSize;
                    //if (copyRect == null) {
                    int iconType = tag.getTagIcon();
                    Color color = tag.getTagColor();

                    if (color != null || iconType != -1) {
                        if (color != null) {
                            Color before = g2d.getColor();
                            g2d.setColor(color);
                            g2d.fill(new Rectangle2D.Double(tagX, tagY, pTagSize, pTagSize));
                            g2d.setColor(before);
                        }

                        if (iconType != -1) {
                            //drawing
                            Image tagImage = ImageManager.getUnitImage(iconType, false).getScaledInstance(pTagSize, pTagSize, Image.SCALE_FAST);
                            g2d.drawImage(tagImage, tagX, tagY, null);
                        }
                        g2d.dispose();
                        AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
                        t.scale(1.0 / zoom, 1.0 / zoom);
                        g2d.drawImage(image, t, null);
                        renderedSpriteBounds.put(tag, new Rectangle(col * pFieldWidth + tagX, row * pFieldHeight + tagY, pFieldWidth, pFieldHeight));
                    }
                    /*} else {
                    g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x + tagX, row * pFieldHeight - copyRect.y + tagY);
                    }*/
                    //calculate positioning
                    cnt++;
                    xcnt++;
                    if (cnt == 2) {
                        //show only 2 icons in the first line to avoid marker overlay
                        xcnt = 1;
                        ycnt--;

                    }
                }
                // </editor-fold>
            }
        }

        /* if (c != null) {
        //village was recently conquered
        g2d.drawImage(mConquerWarning, w - pConquerSize, h - pConquerSize, pConquerSize, pConquerSize, null);
        }*/
        return image;
    }

    public void reset() {
        setRenderedBounds(null);
        setFullRenderRequired(true);
    }
}
