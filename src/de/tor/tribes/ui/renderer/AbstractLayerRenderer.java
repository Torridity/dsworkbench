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
import java.awt.Color;
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
    private boolean fullRenderRequired = true;

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
        RenderSettings settings = getRenderSettings(pVirtualBounds);

        if (fullRenderRequired) {
            mLayer = createEmptyBuffered(pVisibleVillages.length * settings.getFieldWidth(), pVisibleVillages[0].length * settings.getFieldHeight(), BufferedImage.BITMASK);
            settings.setRowsToRender(pVisibleVillages[0].length);
        } else {
            //copy existing data to new location
            performCopy(settings, pVisibleVillages);
            // pG2d.copyArea(cp, cp, dp, cp, dp, dp);
        }
        Graphics2D g2d = (Graphics2D) mLayer.getGraphics();
        //g2d.clearRect(0, 0, mLayer.getWidth(), mLayer.getHeight());
        renderedSpriteBounds = new HashMap<Integer, Rectangle>();
        int[][] rendered = new int[pVisibleVillages.length][pVisibleVillages[0].length];
        //Set new bounds
        mRenderedBounds = (Rectangle2D.Double) pVirtualBounds.clone();
        BufferedImage img = renderRows(pVisibleVillages, settings, rendered);
        long s = System.currentTimeMillis();
        AffineTransform trans = AffineTransform.getTranslateInstance(0, 0);
        if (settings.getRowsToRender() < 0) {
            //  g2d.drawImage(img, (int) Math.floor(settings.getDeltaX()), (int) Math.floor(settings.getDeltaY()), null);
            // } else {
            //pG2d.drawImage(img, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
            //    g2d.drawImage(img, (int) Math.floor(settings.getDeltaX()), (int) Math.floor(settings.getDeltaY()) + (pVisibleVillages[0].length + settings.getRowsToRender()) * settings.getFieldHeight(), null);
            trans.setToTranslation(0, (pVisibleVillages[0].length + settings.getRowsToRender()) * settings.getFieldHeight());
        }
        g2d.drawImage(img, trans, null);
        //pG2d.drawImage(img, trans, null);
        //  System.out.println("DrawRow: " + (System.currentTimeMillis() - s));

        if (fullRenderRequired) {
            //everything was rendered, skip col rendering
            fullRenderRequired = false;
        } else {
            renderedSpriteBounds = new HashMap<Integer, Rectangle>();
            img = renderColumns(pVisibleVillages, settings, rendered);
            s = System.currentTimeMillis();
            trans = AffineTransform.getTranslateInstance(0, 0);
            if (settings.getColumnsToRender() < 0) {
                //g2d.drawImage(img, (int) Math.floor(settings.getDeltaX()), (int) Math.floor(settings.getDeltaY()), null);
                //} else {
                trans.setToTranslation((pVisibleVillages.length + settings.getColumnsToRender()) * settings.getFieldWidth(), 0);
                //  g2d.drawImage(img, (int) Math.floor(settings.getDeltaX()) + (pVisibleVillages.length + settings.getColumnsToRender()) * settings.getFieldWidth(), (int) Math.floor(settings.getDeltaY()), null);
            }
            g2d.drawImage(img, trans, null);
        }


        trans.setToTranslation((int) Math.floor(settings.getDeltaX()), (int) Math.floor(settings.getDeltaY()));
        pG2d.drawImage(mLayer, trans, null);

        // System.out.println("DrawCol: " + (System.currentTimeMillis() - s));
        // System.out.println("Dur: " + (System.currentTimeMillis() - s));
    }

    private RenderSettings getRenderSettings(Rectangle2D pVirtualBounds) {
        RenderSettings settings = new RenderSettings();
        settings.setFieldWidth(GlobalOptions.getSkin().getCurrentFieldWidth());
        settings.setFieldHeight(GlobalOptions.getSkin().getCurrentFieldHeight());
        int xMovement = 0;
        int yMovement = 0;
        if (mRenderedBounds != null) {
            xMovement = (int) Math.floor(settings.getFieldWidth() * (pVirtualBounds.getX() - mRenderedBounds.getX()));
            yMovement = (int) Math.floor(settings.getFieldHeight() * (pVirtualBounds.getY() - mRenderedBounds.getY()));
        }
        double deltaX = 0;
        if (xMovement != 0) {
            deltaX = xMovement / (double) settings.getFieldWidth();
        }
        double deltaY = 0;
        if (yMovement != 0) {
            deltaY = yMovement / (double) settings.getFieldHeight();
        }
        int fieldsX = (deltaX > 0) ? (int) Math.floor(deltaX + 1) : (int) Math.floor(deltaX - 1);
        int fieldsY = (deltaY > 0) ? (int) Math.floor(deltaY + 1) : (int) Math.floor(deltaY - 1);
        settings.setColumnsToRender(fieldsX);
        settings.setRowsToRender(fieldsY);
        settings.setDeltaX(0 - ((pVirtualBounds.getX() - Math.floor(pVirtualBounds.getX())) * settings.getFieldWidth()));
        settings.setDeltaY(0 - ((pVirtualBounds.getY() - Math.floor(pVirtualBounds.getY())) * settings.getFieldHeight()));
        settings.setZoom(MapPanel.getSingleton().getMapRenderer().getCurrentZoom());

        return settings;
    }

    private class RenderSettings {

        private int columnsToRender = 0;
        private int rowsToRender = 0;
        private int fieldWidth = 0;
        private int fieldHeight = 0;
        private double deltaX = 0.0;
        private double deltaY = 0.0;
        private double zoom = 1.0;

        /**
         * @return the fieldWidth
         */
        public int getFieldWidth() {
            return fieldWidth;
        }

        /**
         * @param fieldWidth the fieldWidth to set
         */
        public void setFieldWidth(int fieldWidth) {
            this.fieldWidth = fieldWidth;
        }

        /**
         * @return the fieldHeight
         */
        public int getFieldHeight() {
            return fieldHeight;
        }

        /**
         * @param fieldHeight the fieldHeight to set
         */
        public void setFieldHeight(int fieldHeight) {
            this.fieldHeight = fieldHeight;
        }

        /**
         * @return the deltaX
         */
        public double getDeltaX() {
            return deltaX;
        }

        /**
         * @param deltaX the deltaX to set
         */
        public void setDeltaX(double deltaX) {
            this.deltaX = deltaX;
        }

        /**
         * @return the deltaY
         */
        public double getDeltaY() {
            return deltaY;
        }

        /**
         * @param deltaY the deltaY to set
         */
        public void setDeltaY(double deltaY) {
            this.deltaY = deltaY;
        }

        /**
         * @return the columnsToRender
         */
        public int getColumnsToRender() {
            return columnsToRender;
        }

        /**
         * @param columnsToRender the columnsToRender to set
         */
        public void setColumnsToRender(int columnsToRender) {
            this.columnsToRender = columnsToRender;
        }

        /**
         * @return the rowsToRender
         */
        public int getRowsToRender() {
            return rowsToRender;
        }

        /**
         * @param rowsToRender the rowsToRender to set
         */
        public void setRowsToRender(int rowsToRender) {
            this.rowsToRender = rowsToRender;
        }

        /**
         * @return the zoom
         */
        public double getZoom() {
            return zoom;
        }

        /**
         * @param zoom the zoom to set
         */
        public void setZoom(double zoom) {
            this.zoom = zoom;
        }
    }

    private void performCopy(RenderSettings pSettings, Village[][] pVillages) {
        int cols = pSettings.getColumnsToRender();
        int rows = pSettings.getRowsToRender();
        /* int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pVillages.length - Math.abs(pSettings.getColumnsToRender());
         */
        Graphics2D g2d = (Graphics2D) mLayer.getGraphics();
        g2d.copyArea(0, 0, mLayer.getWidth(), mLayer.getHeight(), pSettings.getColumnsToRender() * pSettings.getFieldWidth(), pSettings.getRowsToRender() * pSettings.getFieldHeight());
    }

    private BufferedImage renderRows(Village[][] pVillages, RenderSettings pSettings, int[][] pRendered) {
        //create new buffer for rendering
        long s = System.currentTimeMillis();
        BufferedImage newRows = createEmptyBuffered(pVillages.length * pSettings.getFieldWidth(), Math.abs(pSettings.getRowsToRender()) * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //  System.out.println("Create: " + (System.currentTimeMillis() - s));
        //calculate first row that will be rendered
        int firstRow = (pSettings.getRowsToRender() > 0) ? 0 : pVillages[0].length - Math.abs(pSettings.getRowsToRender());
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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
            for (int y = firstRow; y < firstRow + Math.abs(pSettings.getRowsToRender()); y++) {
                cnt++;
                pRendered[x][y] = 1;
                Village v = pVillages[x][y];
                int row = y - firstRow;
                int col = x;
                int globalCol = colToGlobalPosition(col);
                int globalRow = rowToGlobalPosition(y);
                renderField(v, row, col, globalRow, globalCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
            }
        }
        //  System.out.println("Row: " + cnt + "/" + dp + "/" + cp + "(" + vs + "," + ds + ") [" + renderedSpriteBounds.size() + "]");
        g2d.dispose();
        // System.out.println("Render: " + (System.currentTimeMillis() - s));
        return newRows;
    }

    private BufferedImage renderColumns(Village[][] pVillages, RenderSettings pSettings, int[][] pRendered) {
        //create new buffer for rendering
        long s = System.currentTimeMillis();
        BufferedImage newColumns = createEmptyBuffered(Math.abs(pSettings.getColumnsToRender()) * pSettings.getFieldWidth(), pVillages[0].length * pSettings.getFieldHeight(), BufferedImage.BITMASK);
        //   System.out.println("Create: " + (System.currentTimeMillis() - s));
        //calculate first row that will be rendered
        int firstCol = (pSettings.getColumnsToRender() > 0) ? 0 : pVillages.length - Math.abs(pSettings.getColumnsToRender());
        Graphics2D g2d = (Graphics2D) newColumns.getGraphics();
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
        for (int x = firstCol; x < firstCol + Math.abs(pSettings.getColumnsToRender()); x++) {
            for (int y = 0; y < pVillages[0].length; y++) {
                cnt++;
                if (pRendered[x][y] != 1) {
                    //iterate from first row for 'pRows' times
                    Village v = pVillages[x][y];
                    int row = y;
                    int col = x - firstCol;
                    int globalCol = colToGlobalPosition(x);
                    int globalRow = rowToGlobalPosition(row);
                    renderField(v, row, col, globalRow, globalCol, pSettings.getFieldWidth(), pSettings.getFieldHeight(), pSettings.getZoom(), useDecoration, g2d);
                }
            }
        }
        //  System.out.println("Col: " + cnt + "/" + dp + "/" + cp + "(" + vs + "," + ds + ") [" + renderedSpriteBounds.size() + "]");
        g2d.dispose();
        //  System.out.println("Render: " + (System.currentTimeMillis() - s));

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
        fullRenderRequired = true;
    }
}
