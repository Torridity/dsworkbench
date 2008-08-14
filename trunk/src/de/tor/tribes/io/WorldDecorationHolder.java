/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 *
 * @author Charon
 */
public class WorldDecorationHolder {

    private static Logger logger = Logger.getLogger(WorldDecorationHolder.class);
    byte[] decoration = new byte[1000000];
    private List<BufferedImage> mTextures;

    public void loadWorld() throws FileNotFoundException, FileFormatException {
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
        } catch (Exception e) {
            logger.error("Failed to read world.dat.gz");
            throw new FileFormatException("world.dat.gz hat ein ungültiges Format. (" + e.getMessage() + ")");
        }
        loadTextures();
    }
    //private List<String> paths = new LinkedList<String>();

    private void loadTextures() throws FileNotFoundException {
        mTextures = new LinkedList<BufferedImage>();
        try {
            mTextures.add(ImageIO.read(new File("./graphics/world/gras1.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/gras2.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/gras3.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/gras4.png")));
            //dummy values
            mTextures.add(ImageIO.read(new File("./graphics/world/gras4.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/gras4.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/gras4.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/gras4.png")));

            mTextures.add(ImageIO.read(new File("./graphics/world/berg1.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/berg2.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/berg3.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/berg4.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/see.png")));
            //dummy values
            mTextures.add(ImageIO.read(new File("./graphics/world/see.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/see.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/see.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0000.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0001.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0010.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0011.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0100.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0101.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0110.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest0111.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1000.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1001.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1010.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1011.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1100.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1101.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1110.png")));
            mTextures.add(ImageIO.read(new File("./graphics/world/forest1111.png")));
        } catch (Exception e) {
            throw new FileNotFoundException("Not all world textures where found");
        }
    }

    public Image getTexture(int pX, int pY, double pScale) {
        return mTextures.get(decoration[pY * 1000 + pX]).getScaledInstance((int) Math.rint(mTextures.get(0).getWidth() / pScale), (int) Math.rint(mTextures.get(0).getHeight() / pScale), BufferedImage.SCALE_FAST);
    }

    public static void main(String[] args) {
        try {
            FileInputStream r = new FileInputStream("world.dat");
            GZIPOutputStream o = new GZIPOutputStream(new FileOutputStream("world.dat.gz"));
            int b = 0;
            byte[] v = new byte[1024];
            while ((b = r.read(v)) != -1) {
                o.write(v, 0, b);
            }
            o.finish();
            o.close();

            WorldDecorationHolder h = new WorldDecorationHolder();
            h.loadWorld();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
