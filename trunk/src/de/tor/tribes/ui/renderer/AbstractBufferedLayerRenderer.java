/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.renderer;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.GlobalOptions;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 *
 * @author Torridity
 */
public abstract class AbstractBufferedLayerRenderer {

    private boolean fullRenderRequired = true;
    private Rectangle2D renderedBounds = null;

    public abstract void performRendering(Rectangle2D pVirtualBounds, Village[][] pVisibleVillages, Graphics2D pG2d);

    protected RenderSettings getRenderSettings(Rectangle2D pVirtualBounds) {
        RenderSettings settings = new RenderSettings(this);
        settings.setFieldWidth(GlobalOptions.getSkin().getCurrentFieldWidth());
        settings.setFieldHeight(GlobalOptions.getSkin().getCurrentFieldHeight());
        double xMovement = 0;
        double yMovement = 0;

        if (getRenderedBounds() != null) {
            xMovement = settings.getFieldWidth() * (getRenderedBounds().getX() - pVirtualBounds.getX());
            yMovement = settings.getFieldHeight() * (getRenderedBounds().getY() - pVirtualBounds.getY());
        }

        double deltaX = 0;
        if (xMovement != 0) {
            deltaX = xMovement / (double) settings.getFieldWidth();
        }
        double deltaY = 0;
        if (yMovement != 0) {
            deltaY = yMovement / (double) settings.getFieldHeight();
        }

        int fieldsX = (deltaX > 0) ? (int) Math.round(deltaX) : (int) Math.floor(deltaX);
        int fieldsY = (deltaY > 0) ? (int) Math.round(deltaY) : (int) Math.floor(deltaY);
        fieldsX += (fieldsX >= 0) ? 1 : -1;
        fieldsY += (fieldsY >= 0) ? 1 : -1;

        settings.setColumnsToRender(fieldsX);
        settings.setRowsToRender(fieldsY);
        if (getRenderedBounds() != null) {
            settings.setDeltaX(0 - ((pVirtualBounds.getX() - Math.floor(pVirtualBounds.getX())) * settings.getFieldWidth()));
            settings.setDeltaY(0 - ((pVirtualBounds.getY() - Math.floor(pVirtualBounds.getY())) * settings.getFieldHeight()));
        } else {
            settings.setDeltaX(0);
            settings.setDeltaY(0);
        }
        settings.setZoom(MapPanel.getSingleton().getMapRenderer().getCurrentZoom());

        return settings;
    }

    /**Create an empty BufferedImage
     * @param w
     * @param h
     * @param trans
     * @return
     */
    protected BufferedImage createEmptyBuffered(int w, int h, int trans) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        BufferedImage buffy = config.createCompatibleImage(w, h, trans);
        return buffy;
    }

    protected VolatileImage createEmptyVolatile(int w, int h, int trans) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        VolatileImage buffy = config.createCompatibleVolatileImage(w, h, trans);
        return buffy;
    }

    /**Optimize an image or create a copy of an image if the image was created by getBufferedImage()*/
    protected BufferedImage optimizeImage(BufferedImage image) {
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

    /**
     * @return the fullRenderRequired
     */
    public boolean isFullRenderRequired() {
        return fullRenderRequired;
    }

    /**
     * @param fullRenderRequired the fullRenderRequired to set
     */
    public void setFullRenderRequired(boolean fullRenderRequired) {
        this.fullRenderRequired = fullRenderRequired;
    }

    /**
     * @return the renderedBounds
     */
    public Rectangle2D getRenderedBounds() {
        return renderedBounds;
    }

    /**
     * @param renderedBounds the renderedBounds to set
     */
    public void setRenderedBounds(Rectangle2D renderedBounds) {
        this.renderedBounds = renderedBounds;
    }
}
