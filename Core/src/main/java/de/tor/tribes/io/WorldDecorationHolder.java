/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.io;

import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ImageUtils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Charon
 */
public class WorldDecorationHolder {

  private static Logger logger = LogManager.getLogger("WorldDecorationManager");
  private static byte[] decoration = new byte[1000000];
  private static List<BufferedImage> mTextures = null;
  private static HashMap<Integer, HashMap<Double, BufferedImage>> cache = new HashMap<>();
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
  private static boolean valid = false;

  public static void initialize() throws Exception {
    try {
      loadWorld();
    } catch (Exception e) {
      valid = false;
      throw e;
    }
  }

  public static boolean isValid() {
    return valid;
  }

  private static void loadWorld() throws Exception {
    try {
      GZIPInputStream fin = new GZIPInputStream(new FileInputStream("world.dat.gz"));
      ByteBuffer bb = ByteBuffer.allocate(1000000);
      byte[] d = new byte[1024];
      int c = 0;
      while ((c = fin.read(d)) != -1) {
        bb.put(d, 0, c);
      }
      decoration = bb.array();
      fin.close();
    } catch (Exception e) {
      logger.error("Failed to read decoration file");
      throw new Exception("Unable to read decoration file", e);
    }
    loadTextures();
  }

  public static void loadTextures() {
    mTextures = new LinkedList<>();
    cache.clear();
    try {
      String skinId = GlobalOptions.getSkin().getSkinID();
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras1.png")));//0
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras2.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras3.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));
      //dummy values
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));//4
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));

      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/berg1.png")));//8
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/berg2.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/berg3.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/berg4.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/see.png")));
      //dummy values
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));//13
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/gras4.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0000.png")));//16
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0001.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0010.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0011.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0100.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0101.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0110.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest0111.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1000.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1001.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1010.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1011.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1100.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1101.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1110.png")));
      mTextures.add(ImageUtils.loadImage(new File("graphics/world/" + skinId + "/forest1111.png")));
      valid = true;
    } catch (Exception e) {
      valid = false;
    }
  }

  public static BufferedImage getOriginalSprite(int pX, int pY) {
    if ((pX < 0) || (pY < 0) || (pX > 999) || (pY > 999)) {
      //return default texture
      return mTextures.get(0);
    }
    int decoId = decoration[pY * 1000 + pX];
    return mTextures.get(decoId);
  }

  public static BufferedImage getCachedImage(int pX, int pY, double pScaling) {
    int decoId = 0;
    if ((pX < 0) || (pY < 0) || (pX > 999) || (pY > 999)) {
      //keep default ID
    } else {
      decoId = decoration[pY * 1000 + pX];
    }
    try {
      HashMap<Double, BufferedImage> imageCache = cache.get(decoId);
      if (imageCache == null) {
        imageCache = new HashMap<>();
        cache.put(decoId, imageCache);
      }

      BufferedImage cached = imageCache.get(pScaling);
      if (cached == null) {
        int iFieldWidth = mTextures.get(decoId).getWidth();
        int iFieldHeight = mTextures.get(decoId).getHeight();
        Image scaled = mTextures.get(decoId).getScaledInstance((int) (iFieldWidth / pScaling), (int) (iFieldHeight / pScaling), BufferedImage.SCALE_FAST);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        cached = gc.createCompatibleImage(scaled.getWidth(null), scaled.getHeight(null), Transparency.BITMASK);
        Graphics2D g = cached.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        imageCache.put(pScaling, cached);
      }
      return cached;
    } catch (Exception e) {
      return null;
    }
  }

  public static Image getTexture(int pX, int pY, double pScale) {
    if ((pX < 0) || (pY < 0) || (pX > 999) || (pY > 999)) {
      //return default texture
      return mTextures.get(0);
    }
    int decoId = decoration[pY * 1000 + pX];
    HashMap<Double, BufferedImage> cacheForId = cache.get(decoId);

    if (cacheForId == null) {
      cacheForId = new HashMap<>();
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
