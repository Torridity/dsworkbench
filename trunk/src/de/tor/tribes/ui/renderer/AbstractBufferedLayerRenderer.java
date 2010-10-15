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
