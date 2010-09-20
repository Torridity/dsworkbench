/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 *
 * @author Torridity
 */
public abstract class AbstractLayerRenderer {

    private BufferedImage mLayer = null;
    private Rectangle2D mRenderedBounds = null;
    private HashMap<Integer, Rectangle> renderedSpriteBounds = null;

    /**Create an empty BufferedImage
     * @param w
     * @param h
     * @param trans
     * @return
     */
    public BufferedImage createEmptyBuffered(int w, int h, int trans) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        BufferedImage buffy = config.createCompatibleImage(w, h, trans);
        return optimizeImage(buffy);
    }

    /**Optimize an image or create a copy of an image if the image was created by getBufferedImage()*/
    private BufferedImage optimizeImage(BufferedImage image) {
        // obtain the current system graphical settings
        GraphicsConfiguration gfx_config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfx_config.getColorModel())) {
            return image;
        }

        // image is not optimized, so create a new image that is
        BufferedImage new_image = gfx_config.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) new_image.getGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return new_image;
    }

    public void prepareRender(Rectangle2D pVirtualBounds, Village[][] pVisibleVillages, Graphics2D pG2d) {
        int xMovement = 0;
        int yMovement = 0;

        int wField = GlobalOptions.getSkin().getCurrentFieldWidth();
        int hField = GlobalOptions.getSkin().getCurrentFieldHeight();
        if (mRenderedBounds != null) {
            xMovement = (int) Math.floor(wField * (pVirtualBounds.getX() - mRenderedBounds.getX()));
            yMovement = (int) Math.floor(hField * (pVirtualBounds.getY() - mRenderedBounds.getY()));
        }

        mRenderedBounds = (Rectangle2D.Double) pVirtualBounds.clone();

        double deltaX = 0;
        if (xMovement != 0) {
            deltaX = xMovement / (double) wField;
        }
        double deltaY = 0;
        if (yMovement != 0) {
            deltaY = yMovement / (double) hField;
        }

        int fieldsX = (deltaX > 0) ? (int) Math.floor(deltaX + 1) : (int) Math.floor(deltaX - 1);
        int fieldsY = (deltaY > 0) ? (int) Math.floor(deltaY + 1) : (int) Math.floor(deltaY - 1);

        double xPos = pVirtualBounds.getX();
        double yPos = pVirtualBounds.getY();
        double dx = 0 - ((xPos - Math.floor(xPos)) * wField);
        double dy = 0 - ((yPos - Math.floor(yPos)) * hField);
        long s = System.currentTimeMillis();
        renderedSpriteBounds = new HashMap<Integer, Rectangle>();
        BufferedImage img = renderRows(pVisibleVillages, fieldsY, wField, hField);
        if (fieldsY > 0) {
            pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy), null);
        } else {
            //pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
            pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
        }
        renderedSpriteBounds = new HashMap<Integer, Rectangle>();
        img = renderColumns(pVisibleVillages, fieldsX, wField, hField);
        if (fieldsX > 0) {
            pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy), null);
        } else {
            //pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
            pG2d.drawImage(img, (int) Math.floor(dx) + (pVisibleVillages.length + fieldsX) * wField, (int) Math.floor(dy), null);
        }
        System.out.println("Dur: " + (System.currentTimeMillis() - s));
    }

    private BufferedImage renderRows(Village[][] pVillages, int pRows, int pFieldWidth, int pFieldHeight) {
        //create new buffer for rendering
        BufferedImage newRows = createEmptyBuffered(pVillages.length * pFieldWidth, Math.abs(pRows) * pFieldHeight, BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstRow = (pRows > 0) ? 0 : pVillages[0].length - Math.abs(pRows);
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        double zoom = MapPanel.getSingleton().getMapRenderer().getCurrentZoom();
        //iterate through entire row
        int cnt = 0;
        int cp = 0;
        for (int x = 0; x < pVillages.length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pRows); y++) {
                cnt++;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;
                Image sprite = null;
                Rectangle copyRect = null;
                int textureId = -1;
                if (v != null) {
                    //village field that has to be rendered
                    if (!GlobalOptions.getSkin().isMinimapSkin()) {
                        textureId = v.getGraphicsType();
                        copyRect = renderedSpriteBounds.get(textureId);
                        if (copyRect == null) {
                            sprite = GlobalOptions.getSkin().getImage(textureId, zoom);
                        }
                    } else {
                        g2d.setColor(Color.MAGENTA);
                        textureId = 0;
                        copyRect = renderedSpriteBounds.get(textureId);
                        if (copyRect == null) {
                            g2d.drawRect(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1);
                            renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1));
                        }
                    }
                } else {
                    int globalCol = colToGlobalPosition(col);
                    int globalRow = rowToGlobalPosition(y);
                    if (!GlobalOptions.getSkin().isMinimapSkin()) {
                        textureId = WorldDecorationHolder.getTextureId(globalCol, globalRow) + 100;
                        copyRect = renderedSpriteBounds.get(textureId + 100);
                        if (copyRect == null) {
                            sprite = WorldDecorationHolder.getTexture(globalCol, globalRow, MapPanel.getSingleton().getMapRenderer().getCurrentZoom());
                        }
                    } else {
                        g2d.setColor(Color.MAGENTA);
                        textureId = 1;
                        copyRect = renderedSpriteBounds.get(textureId);
                        if (copyRect == null) {
                            g2d.fillRect(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1);
                            renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1));
                        }
                    }
                }

                if (sprite != null) {
                    //render sprite
                    g2d.drawImage(sprite, col * pFieldWidth, row * pFieldHeight, null);
                    renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight));
                } else if (copyRect != null) {
                    //copy from copy rect
                    cp++;
                    g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x, row * pFieldHeight - copyRect.y);
                }
            }
        }
        System.out.println("Row: " + cnt);
        System.out.println("CP : " + cp);
        return newRows;
    }

    private BufferedImage renderColumns(Village[][] pVillages, int pColumns, int pFieldWidth, int pFieldHeight) {
        //create new buffer for rendering
        BufferedImage newColumns = createEmptyBuffered(Math.abs(pColumns) * pFieldWidth, pVillages[0].length * pFieldHeight, BufferedImage.BITMASK);
        //calculate first row that will be rendered
        int firstCol = (pColumns > 0) ? 0 : pVillages.length - Math.abs(pColumns);
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        double zoom = MapPanel.getSingleton().getMapRenderer().getCurrentZoom();
        //iterate through entire row
        int cnt = 0;
        int cp = 0;
        for (int x = firstCol; x < firstCol + Math.abs(pColumns); x++) {
            for (int y = 0; y < pVillages[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pVillages[x][y];
                int row = y;
                int col = x - firstCol;
                Image sprite = null;
                Rectangle copyRect = null;
                int textureId = -1;
                if (v != null) {
                    //village field that has to be rendered
                    if (!GlobalOptions.getSkin().isMinimapSkin()) {
                        textureId = v.getGraphicsType();
                        copyRect = renderedSpriteBounds.get(textureId);
                        if (copyRect == null) {
                            sprite = GlobalOptions.getSkin().getImage(textureId, zoom);
                        }
                    } else {
                        g2d.setColor(Color.MAGENTA);
                        textureId = 0;
                        copyRect = renderedSpriteBounds.get(textureId);
                        if (copyRect == null) {
                            g2d.drawRect(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1);
                            renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1));
                        }
                    }
                } else {
                    //decoration has to be rendered
                    int globalCol = colToGlobalPosition(x);
                    int globalRow = rowToGlobalPosition(row);
                    if (!GlobalOptions.getSkin().isMinimapSkin()) {
                        textureId = WorldDecorationHolder.getTextureId(globalCol, globalRow);
                        copyRect = renderedSpriteBounds.get(textureId + 100);
                        if (copyRect == null) {
                            sprite = WorldDecorationHolder.getTexture(globalCol, globalRow, zoom);
                        }
                    } else {
                        g2d.setColor(Color.MAGENTA);
                        textureId = 1;
                        copyRect = renderedSpriteBounds.get(textureId);
                        if (copyRect == null) {
                            g2d.fillRect(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1);
                            renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth - 1, pFieldHeight - 1));
                        }
                    }
                }

                if (sprite != null) {
                    g2d.drawImage(sprite, col * pFieldWidth, row * pFieldHeight, null);
                    renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight));
                } else if (copyRect != null) {
                    //copy from copy rect
                    cp++;
                    g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x, row * pFieldHeight - copyRect.y);
                }
            }
        }
        System.out.println("Col: " + cnt);
        System.out.println("CP : " + cp);
        return newColumns;
    }

    private int rowToGlobalPosition(int pRow) {
        int yPos = (int) Math.floor(mRenderedBounds.getY());
        return yPos + pRow;
    }

    private int colToGlobalPosition(int pCol) {
        int xPos = (int) Math.floor(mRenderedBounds.getX());
        return xPos + pCol;
    }

    public void reset() {
        mRenderedBounds = null;
    }
}
