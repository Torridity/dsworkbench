/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.Skin;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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
    boolean fullyRendered = false;

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

        renderedSpriteBounds = new HashMap<Integer, Rectangle>();
       /* if (!fullyRendered) {
            BufferedImage img = renderRows(pVisibleVillages, pVisibleVillages[0].length, wField, hField);
            if (fieldsY > 0) {
                pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy), null);
            } else {
                //pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
                pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
            }
            return;
        }*/

        BufferedImage img = renderRows(pVisibleVillages, fieldsY, wField, hField);
        long s = System.currentTimeMillis();
        if (fieldsY > 0) {
            pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy), null);
        } else {
            //pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
            pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
        }
        System.out.println("DrawRow: " + (System.currentTimeMillis() - s));

        renderedSpriteBounds = new HashMap<Integer, Rectangle>();
        img = renderColumns(pVisibleVillages, fieldsX, wField, hField);
        s = System.currentTimeMillis();
        if (fieldsX > 0) {
            pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy), null);
        } else {
            pG2d.drawImage(img, (int) Math.floor(dx) + (pVisibleVillages.length + fieldsX) * wField, (int) Math.floor(dy), null);
        }
        System.out.println("DrawCol: " + (System.currentTimeMillis() - s));
        System.out.println("Dur: " + (System.currentTimeMillis() - s));
    }

    private BufferedImage renderRows(Village[][] pVillages, int pRows, int pFieldWidth, int pFieldHeight) {
        //create new buffer for rendering
        long s = System.currentTimeMillis();
        BufferedImage newRows = createEmptyBuffered(pVillages.length * pFieldWidth, Math.abs(pRows) * pFieldHeight, BufferedImage.BITMASK);
        System.out.println("Create: " + (System.currentTimeMillis() - s));
        //calculate first row that will be rendered
        int firstRow = (pRows > 0) ? 0 : pVillages[0].length - Math.abs(pRows);
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double zoom = MapPanel.getSingleton().getMapRenderer().getCurrentZoom();
        //iterate through entire row
        int cnt = 0;
        cp = 0;
        dp = 0;
        vs = 0;
        ds = 0;
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        s = System.currentTimeMillis();
        for (int x = 0; x < pVillages.length; x++) {
            //iterate from first row for 'pRows' times
            for (int y = firstRow; y < firstRow + Math.abs(pRows); y++) {
                cnt++;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;
                int globalCol = colToGlobalPosition(col);
                int globalRow = rowToGlobalPosition(y);
                renderField(v, row, col, globalRow, globalCol, pFieldWidth, pFieldHeight, zoom, useDecoration, g2d);
            }
        }
        System.out.println("Row: " + cnt + "/" + dp + "/" + cp + "(" + vs + "," + ds + ") [" + renderedSpriteBounds.size() + "]");
        g2d.dispose();
        System.out.println("Render: " + (System.currentTimeMillis() - s));
        return newRows;
    }

    private BufferedImage renderColumns(Village[][] pVillages, int pColumns, int pFieldWidth, int pFieldHeight) {
        //create new buffer for rendering
        long s = System.currentTimeMillis();
        BufferedImage newColumns = createEmptyBuffered(Math.abs(pColumns) * pFieldWidth, pVillages[0].length * pFieldHeight, BufferedImage.BITMASK);
        System.out.println("Create: " + (System.currentTimeMillis() - s));
        //calculate first row that will be rendered
        int firstCol = (pColumns > 0) ? 0 : pVillages.length - Math.abs(pColumns);
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
        double zoom = MapPanel.getSingleton().getMapRenderer().getCurrentZoom();
        //iterate through entire row
        int cnt = 0;
        cp = 0;
        dp = 0;
        vs = 0;
        ds = 0;
        boolean useDecoration = true;

        if ((WorldDecorationHolder.getTexture(0, 0, 1).getWidth(null) != GlobalOptions.getSkin().getBasicFieldWidth()) || (WorldDecorationHolder.getTexture(0, 0, 1).getHeight(null) != GlobalOptions.getSkin().getBasicFieldHeight())) {
            //use decoration if skin field size equals the world skin size
            useDecoration = false;
        }
        s = System.currentTimeMillis();
        for (int x = firstCol; x < firstCol + Math.abs(pColumns); x++) {
            for (int y = 0; y < pVillages[0].length; y++) {
                cnt++;
                //iterate from first row for 'pRows' times
                Village v = pVillages[x][y];
                int row = y;
                int col = x - firstCol;
                int globalCol = colToGlobalPosition(x);
                int globalRow = rowToGlobalPosition(row);
                renderField(v, row, col, globalRow, globalCol, pFieldWidth, pFieldHeight, zoom, useDecoration, g2d);
            }
        }
        System.out.println("Col: " + cnt + "/" + dp + "/" + cp + "(" + vs + "," + ds + ") [" + renderedSpriteBounds.size() + "]");
        g2d.dispose();
        System.out.println("Render: " + (System.currentTimeMillis() - s));

        return newColumns;
    }
    int cp = 0;
    int dp = 0;
    int vs = 0;
    int ds = 0;

    private void renderField(Village v, int row, int col, int globalRow, int globalCol, int pFieldWidth, int pFieldHeight, double zoom, boolean useDecoration, Graphics2D g2d) {
        // Image sprite = null;
        Rectangle copyRect = null;
        int textureId = -1;
        BufferedImage sprite = null;
        if (v != null) {
            vs++;
            //village field that has to be rendered
            textureId = v.getGraphicsType();
            copyRect = renderedSpriteBounds.get(textureId);
            if (copyRect == null) {
                //sprite = GlobalOptions.getSkin().getImage(textureId, zoom);
                sprite = GlobalOptions.getSkin().getOriginalSprite(textureId);
            }
        } else {
            ds++;
            if (useDecoration) {
                textureId = WorldDecorationHolder.getTextureId(globalCol, globalRow) + 100;
            } else {
                textureId = Skin.ID_DEFAULT_UNDERGROUND + 100;
            }

            copyRect = renderedSpriteBounds.get(textureId);
            if (copyRect == null) {
                // sprite = WorldDecorationHolder.getTexture(globalCol, globalRow, MapPanel.getSingleton().getMapRenderer().getCurrentZoom());
                sprite = WorldDecorationHolder.getOriginalSprite(globalCol, globalRow);
            }
        }

        if (sprite != null) {
            //render sprite
            dp++;
            AffineTransform t = AffineTransform.getTranslateInstance(col * pFieldWidth, row * pFieldHeight);
            t.scale(1 / zoom, 1 / zoom);
            g2d.drawImage(sprite, t, null);
            //g2d.drawImage(sprite, col * pFieldWidth, row * pFieldHeight, null);
            renderedSpriteBounds.put(textureId, new Rectangle(col * pFieldWidth, row * pFieldHeight, pFieldWidth, pFieldHeight));
        } else if (copyRect != null) {
            //copy from copy rect
            cp++;
            g2d.copyArea(copyRect.x, copyRect.y, copyRect.width, copyRect.height, col * pFieldWidth - copyRect.x, row * pFieldHeight - copyRect.y);
        }
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
