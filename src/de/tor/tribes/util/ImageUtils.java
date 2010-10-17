/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 *
 * @author Jejkal
 */
public class ImageUtils {

    public static void setupGraphics(Graphics2D pG2d) {
        pG2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // Speed
        pG2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        pG2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        pG2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        pG2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
        pG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    /**Create an empty BufferedImage
     * @param w
     * @param h
     * @param trans
     * @return
     */
    public static BufferedImage createCompatibleBufferedImage(int w, int h, int trans) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        BufferedImage buffy = config.createCompatibleImage(w, h, trans);
        return buffy;
    }

    /**Create an empty VolatileImage
     * @param w
     * @param h
     * @param trans
     * @return
     */
    public static VolatileImage createCompatibleVolatileImage(int w, int h, int trans) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();

        VolatileImage buffy = config.createCompatibleVolatileImage(w, h, trans);
        return buffy;
    }

    /**Optimize an image or create a copy of an image if the image was created by getBufferedImage()*/
    public static BufferedImage optimizeImage(BufferedImage image) {
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
        g2d.drawRenderedImage(image, AffineTransform.getTranslateInstance(0, 0));
        g2d.dispose();

        // return the new optimized image
        return new_image;
    }

    /**Optimize an image or create a copy of an image if the image was created by getBufferedImage()*/
    public static BufferedImage createOptimizedCopy(BufferedImage image) {
        // image is not optimized, so create a new image that is
        BufferedImage new_image = createCompatibleBufferedImage(image.getWidth(), image.getHeight(), image.getTransparency());
        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = new_image.createGraphics();
        // actually draw the image and dispose of context no longer needed
        g2d.drawRenderedImage(image, AffineTransform.getTranslateInstance(0, 0));
        g2d.dispose();

        // return the new optimized image
        return new_image;
    }
}
