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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Torridity
 */
public abstract class AbstractLayerRenderer {

    private BufferedImage mLayer = null;
    private Rectangle2D mRenderedBounds = null;

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
        return buffy;
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
        int startX = 0;
        int startY = 0;
        int endX = 0;
        int endY = 0;

        double xPos = pVirtualBounds.getX();
        double yPos = pVirtualBounds.getY();
        int upperLeftX = (int) Math.floor(xPos);
        int upperLeftY = (int) Math.floor(yPos);
        int lowerRightX = upperLeftX + pVisibleVillages.length;
        int lowerRightY = upperLeftY + pVisibleVillages[0].length;


        if (fieldsX == 0) {
            startX = 0;
            endX = 0;
        } else if (fieldsX > 0) {
            //left columns has to be redrawn
            startX = 0;
            endX = fieldsX;
            upperLeftX = (int) Math.floor(xPos);
            lowerRightX = upperLeftX + fieldsX;
        } else {
            //right columns has to be redrawn
            startX = pVisibleVillages.length + fieldsX;
            endX = pVisibleVillages.length;
            upperLeftX = upperLeftX + pVisibleVillages.length + fieldsX;
            lowerRightX = upperLeftX + pVisibleVillages.length;
        }

        if (fieldsY == 0) {
            startY = 0;
            endY = 0;
        } else if (fieldsY > 0) {
            //upper rows has to be redrawn
            startY = 0;
            endY = fieldsY;
            upperLeftY = (int) Math.floor(yPos);
            lowerRightY = upperLeftY + fieldsY;
        } else {
            //lower rows has to be redrawn
            startY = pVisibleVillages[0].length + fieldsY;
            endY = pVisibleVillages[0].length;
            upperLeftY = upperLeftY + pVisibleVillages[0].length + fieldsY;
            lowerRightY = upperLeftY + pVisibleVillages[0].length;
        }

        //render rows

        double dx = 0 - ((xPos - Math.floor(xPos)) * wField);
        double dy = 0 - ((yPos - Math.floor(yPos)) * hField);
        int globalX = upperLeftX;
        int globalY = upperLeftY;

        BufferedImage newRows = createEmptyBuffered(pVisibleVillages.length * wField, Math.abs(endY - startY) * hField, BufferedImage.BITMASK);
        Graphics2D g2d = (Graphics2D) newRows.getGraphics();
        int tmpX = 0;
        int tmpY = 0;
        // long s = System.currentTimeMillis();
        for (int x = 0; x < pVisibleVillages.length; x++) {
            for (int y = startY; y < endY; y++) {
                Village v = pVisibleVillages[x][y];
                if (v != null) {
                    int gt = v.getGraphicsType();
                    //g2d.drawImage(GlobalOptions.getSkin().getImage(gt, MapPanel.getSingleton().getMapRenderer().getCurrentZoom()), tmpX * wField, tmpY * hField, null);
                    g2d.setColor(Color.MAGENTA);
                    g2d.drawRect(tmpX * wField, tmpY * hField, wField - 1, hField - 1);
                } else {
                    //  g2d.drawImage(WorldDecorationHolder.getTexture(globalX, globalY, MapPanel.getSingleton().getMapRenderer().getCurrentZoom()), tmpX * wField, tmpY * hField, null);
                }
                globalY++;
                tmpY++;
            }
            globalX++;
            globalY = upperLeftY;
            tmpY = 0;
            tmpX++;
        }
        // System.out.println("D " + (System.currentTimeMillis() - s));
        if (startY == 0) {
            pG2d.drawImage(newRows, (int) Math.floor(dx), (int) Math.floor(dy), null);
        } else {
            pG2d.drawImage(newRows, (int) Math.floor(dx), (int) Math.floor(dy) + (pVisibleVillages[0].length + fieldsY) * hField, null);
        }
        /*//render cols
        for (int x = startX; x < endX; x++) {
        for (int y = 0; y < pVisibleVillages[0].length; y++) {
        Village v = pVisibleVillages[x][y];
        if (v != null) {
        System.out.println("Redraw " + v);
        }
        }
        }*/


    }

    public void reset() {
        mRenderedBounds = null;
    }
}
