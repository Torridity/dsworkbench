/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import sun.java2d.SunGraphics2D;

/**
 *
 * @author Charon
 */
public class WorldDecorationHolder {

    private static Logger logger = Logger.getLogger("WorldDecorationManager");
    private static byte[] decoration = new byte[1000000];
    private static List<BufferedImage> mTextures = null;
    private static HashMap<Integer, HashMap<Double, BufferedImage>> cache = new HashMap<Integer, HashMap<Double, BufferedImage>>();
    public static int ID_GRAS1 = 0;
    public static int ID_GRAS2 = 1;
    public static int ID_GRAS3 = 2;
    public static int ID_GRAS4 = 3;
    public static int ID_ROCK1 = 8;
    public static int ID_ROCK2 = 9;
    public static int ID_ROCK3 = 10;
    public static int ID_ROCK4 = 11;
    public static int ID_SEA = 12;
    public static int ID_FORREST1 = 16;
    public static int ID_FORREST2 = 17;
    public static int ID_FORREST3 = 18;
    public static int ID_FORREST4 = 19;
    public static int ID_FORREST5 = 20;
    public static int ID_FORREST6 = 21;
    public static int ID_FORREST7 = 22;
    public static int ID_FORREST8 = 23;
    public static int ID_FORREST9 = 24;
    public static int ID_FORREST10 = 25;
    public static int ID_FORREST11 = 26;
    public static int ID_FORREST12 = 27;
    public static int ID_FORREST13 = 28;
    public static int ID_FORREST14 = 29;
    public static int ID_FORREST15 = 30;
    public static int ID_FORREST16 = 31;

    public static void initialize() throws FileNotFoundException, Exception {
        loadWorld();
    }

    private static void loadWorld() throws FileNotFoundException, Exception {
        try {
            GZIPInputStream fin = new GZIPInputStream(new FileInputStream("world.dat.gz"));
            // FileInputStream fin = new FileInputStream("world.dat");
            ByteBuffer bb = ByteBuffer.allocate(1000000);
            byte[] d = new byte[1024];
            int c = 0;
            while ((c = fin.read(d)) != -1) {
                bb.put(d, 0, c);
            }
            decoration = bb.array();
            fin.close();
        } catch (Exception e) {
            logger.error("Failed to read world.dat.gz");
            throw new Exception("Unable to read world.dat.gz", e);
        }
        loadTextures();
    }

    private static void loadTextures() throws Exception {
        mTextures = new LinkedList<BufferedImage>();
        try {
            mTextures.add(loadImage(new File("graphics/world/gras1.png")));//0
            mTextures.add(loadImage(new File("graphics/world/gras2.png")));
            mTextures.add(loadImage(new File("graphics/world/gras3.png")));
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));
            //dummy values
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));//4
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));

            mTextures.add(loadImage(new File("graphics/world/berg1.png")));//8
            mTextures.add(loadImage(new File("graphics/world/berg2.png")));
            mTextures.add(loadImage(new File("graphics/world/berg3.png")));
            mTextures.add(loadImage(new File("graphics/world/berg4.png")));
            mTextures.add(loadImage(new File("graphics/world/see.png")));
            //dummy values
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));//13
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));
            mTextures.add(loadImage(new File("graphics/world/gras4.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0000.png")));//16
            mTextures.add(loadImage(new File("graphics/world/forest0001.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0010.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0011.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0100.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0101.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0110.png")));
            mTextures.add(loadImage(new File("graphics/world/forest0111.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1000.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1001.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1010.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1011.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1100.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1101.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1110.png")));
            mTextures.add(loadImage(new File("graphics/world/forest1111.png")));
        } catch (Exception e) {
            throw new Exception("Failed to load world textures", e);
        }
    }

    public static BufferedImage loadImage(File pFile) throws Exception {
        BufferedImage im = ImageIO.read(pFile);

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        BufferedImage buffy = config.createCompatibleImage(im.getWidth(), im.getHeight(), im.getTransparency());
        buffy.getGraphics().drawImage(im, 0, 0, null);
        return buffy;
    }

    public static BufferedImage getOriginalSprite(int pX, int pY) {
        if ((pX < 0) || (pY < 0) || (pX > 999) || (pY > 999)) {
            //return default texture
            return mTextures.get(0);


        }
        int decoId = decoration[pY * 1000 + pX];


        return mTextures.get(decoId);


    }

    public static Image getTexture(int pX, int pY, double pScale) {
        if ((pX < 0) || (pY < 0) || (pX > 999) || (pY > 999)) {
            //return default texture
            return mTextures.get(0);


        }
        int decoId = decoration[pY * 1000 + pX];
        HashMap<Double, BufferedImage> cacheForId = cache.get(decoId);


        if (cacheForId == null) {
            cacheForId = new HashMap<Double, BufferedImage>();
            cache.put(decoId, cacheForId);


        }
        BufferedImage cached = cacheForId.get(pScale);


        if (cached == null) {
            Image scaled = mTextures.get(decoId).getScaledInstance((int) Math.rint(mTextures.get(0).getWidth() / pScale), (int) Math.rint(mTextures.get(0).getHeight() / pScale), BufferedImage.SCALE_DEFAULT);
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            cached = gc.createCompatibleImage(scaled.getWidth(null), scaled.getHeight(null), Transparency.OPAQUE);
            Graphics2D g = cached.createGraphics();
            g.drawImage(scaled, 0, 0, null);
            g.dispose();
            cacheForId.put(pScale, cached);


        }
        return cached;


    }

    public static byte getTextureId(int pX, int pY) {
        if ((pX < 0) || (pY < 0) || (pX > 999) || (pY > 999)) {
            return 0;


        }
        return decoration[pY * 1000 + pX];

    }
}
