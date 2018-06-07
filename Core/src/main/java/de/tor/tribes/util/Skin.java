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
package de.tor.tribes.util;

import de.tor.tribes.io.WorldDecorationHolder;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.ui.windows.SkinPreviewFrame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Charon
 */
public class Skin {

    private static Logger logger = LogManager.getLogger("TexturePack");
    public final static String MINIMAP_SKIN_ID = "minimap";
    private int iFieldWidth = 0;
    private int iFieldHeight = 0;
    /**Texture IDs*/
    public static int ID_DEFAULT_UNDERGROUND = 0;
    public static int ID_V1 = 1;
    public static int ID_V2 = 2;
    public static int ID_V3 = 3;
    public static int ID_V4 = 4;
    public static int ID_V5 = 5;
    public static int ID_V6 = 6;
    public static int ID_V1_LEFT = 7;
    public static int ID_V2_LEFT = 8;
    public static int ID_V3_LEFT = 9;
    public static int ID_V4_LEFT = 10;
    public static int ID_V5_LEFT = 11;
    public static int ID_V6_LEFT = 12;
    public static int ID_B1 = 13;
    public static int ID_B2 = 14;
    public static int ID_B3 = 15;
    public static int ID_B4 = 16;
    public static int ID_B5 = 17;
    public static int ID_B6 = 18;
    public static int ID_B1_LEFT = 19;
    public static int ID_B2_LEFT = 20;
    public static int ID_B3_LEFT = 21;
    public static int ID_B4_LEFT = 22;
    public static int ID_B5_LEFT = 23;
    public static int ID_B6_LEFT = 24;
    /**Filenames*/
    private static final String V1_FILE = "v1.png";
    private static final String V2_FILE = "v2.png";
    private static final String V3_FILE = "v3.png";
    private static final String V4_FILE = "v4.png";
    private static final String V5_FILE = "v5.png";
    private static final String V6_FILE = "v6.png";
    private static final String V1_LEFT_FILE = "v1_left.png";
    private static final String V2_LEFT_FILE = "v2_left.png";
    private static final String V3_LEFT_FILE = "v3_left.png";
    private static final String V4_LEFT_FILE = "v4_left.png";
    private static final String V5_LEFT_FILE = "v5_left.png";
    private static final String V6_LEFT_FILE = "v6_left.png";
    private static final String B1_FILE = "b1.png";
    private static final String B2_FILE = "b2.png";
    private static final String B3_FILE = "b3.png";
    private static final String B4_FILE = "b4.png";
    private static final String B5_FILE = "b5.png";
    private static final String B6_FILE = "b6.png";
    private static final String B1_LEFT_FILE = "b1_left.png";
    private static final String B2_LEFT_FILE = "b2_left.png";
    private static final String B3_LEFT_FILE = "b3_left.png";
    private static final String B4_LEFT_FILE = "b4_left.png";
    private static final String B5_LEFT_FILE = "b5_left.png";
    private static final String B6_LEFT_FILE = "b6_left.png";
    private static final String DEFAULT_UNDERGROUND = "default_underground.png";
    private static final int TEXTURE_COUNT = 25;
    private static String BASE_PATH = "graphics/skins";
    private HashMap<Integer, BufferedImage> mTextures = null;
    private String sSkinID = null;
    private HashMap<Integer, HashMap<Double, BufferedImage>> cache = new HashMap<>();

    public Skin() throws Exception {
        loadSkin("default");
    }

    public Skin(String pSkinPath) throws Exception {
        if (pSkinPath == null || !pSkinPath.equals(MINIMAP_SKIN_ID)) {
            loadSkin(pSkinPath);
            try {
                WorldDecorationHolder.loadTextures();
            } catch (Exception ignored) {
            }
        } else {
            loadMinimapSkin();
            try {
                WorldDecorationHolder.loadTextures();
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isMinimapSkin() {
        try {
            return (sSkinID.equals(MINIMAP_SKIN_ID));
        } catch (Exception ignored) {
        }
        return false;
    }

    /**Get the list of available skins*/
    public static String[] getAvailableSkins() {
        return new File(BASE_PATH).list();
    }

    public String getPreviewFile() {
        return BASE_PATH + "/" + sSkinID + "/preview.png";
    }

    public static void showPreview(String pSkinID, Point pPos) {
        try {
            SkinPreviewFrame f = new SkinPreviewFrame(new Skin(pSkinID));
            f.setLocation(pPos);
            f.setVisible(true);
        } catch (Exception e) {
            JOptionPaneHelper.showInformationBox(DSWorkbenchSettingsDialog.getSingleton(), "Keine Vorschau verfügbar.", "Informatione");
        }
    }

    public String getSkinID() {
        return sSkinID;
    }

    private void loadMinimapSkin() {
        sSkinID = MINIMAP_SKIN_ID;
        iFieldWidth = 10;
        iFieldHeight = 10;
        mTextures = new HashMap<>();
        cache.clear();
        for (int i = 0; i < 25; i++) {
            //BufferedImage image = new BufferedImage(iFieldWidth, iFieldHeight, BufferedImage.TYPE_INT_ARGB);
            BufferedImage image = ImageUtils.createCompatibleBufferedImage(iFieldWidth, iFieldHeight, BufferedImage.BITMASK);
            Graphics2D g2d = image.createGraphics();
            ImageUtils.setupGraphics(g2d);
            if (i == 0) {
                g2d.setColor(Constants.DS_DEFAULT_BACKGROUND);
                g2d.fillRect(0, 0, 10, 10);
            }
            g2d.dispose();
            mTextures.put(i, image);
        }
    }

    private void loadSkin(String pSkinID) throws Exception {
        if (pSkinID == null) {
            pSkinID = "default";
        }

        sSkinID = pSkinID;
        String path = BASE_PATH + "/" + pSkinID;
        mTextures = new HashMap<>();
        cache.clear();
        try {
            mTextures.put(ID_DEFAULT_UNDERGROUND, ImageUtils.loadImage(new File(path + "/" + DEFAULT_UNDERGROUND)));
            iFieldWidth = mTextures.get(0).getWidth(null);
            iFieldHeight = mTextures.get(0).getHeight(null);
            mTextures.put(ID_V1, ImageUtils.loadImage(new File(path + "/" + V1_FILE)));
            mTextures.put(ID_V2, ImageUtils.loadImage(new File(path + "/" + V2_FILE)));
            mTextures.put(ID_V3, ImageUtils.loadImage(new File(path + "/" + V3_FILE)));
            mTextures.put(ID_V4, ImageUtils.loadImage(new File(path + "/" + V4_FILE)));
            mTextures.put(ID_V5, ImageUtils.loadImage(new File(path + "/" + V5_FILE)));
            mTextures.put(ID_V6, ImageUtils.loadImage(new File(path + "/" + V6_FILE)));
            mTextures.put(ID_V1_LEFT, ImageUtils.loadImage(new File(path + "/" + V1_LEFT_FILE)));
            mTextures.put(ID_V2_LEFT, ImageUtils.loadImage(new File(path + "/" + V2_LEFT_FILE)));
            mTextures.put(ID_V3_LEFT, ImageUtils.loadImage(new File(path + "/" + V3_LEFT_FILE)));
            mTextures.put(ID_V4_LEFT, ImageUtils.loadImage(new File(path + "/" + V4_LEFT_FILE)));
            mTextures.put(ID_V5_LEFT, ImageUtils.loadImage(new File(path + "/" + V5_LEFT_FILE)));
            mTextures.put(ID_V6_LEFT, ImageUtils.loadImage(new File(path + "/" + V6_LEFT_FILE)));
            mTextures.put(ID_B1, ImageUtils.loadImage(new File(path + "/" + B1_FILE)));
            mTextures.put(ID_B2, ImageUtils.loadImage(new File(path + "/" + B2_FILE)));
            mTextures.put(ID_B3, ImageUtils.loadImage(new File(path + "/" + B3_FILE)));
            mTextures.put(ID_B4, ImageUtils.loadImage(new File(path + "/" + B4_FILE)));
            mTextures.put(ID_B5, ImageUtils.loadImage(new File(path + "/" + B5_FILE)));
            mTextures.put(ID_B6, ImageUtils.loadImage(new File(path + "/" + B6_FILE)));
            mTextures.put(ID_B1_LEFT, ImageUtils.loadImage(new File(path + "/" + B1_LEFT_FILE)));
            mTextures.put(ID_B2_LEFT, ImageUtils.loadImage(new File(path + "/" + B2_LEFT_FILE)));
            mTextures.put(ID_B3_LEFT, ImageUtils.loadImage(new File(path + "/" + B3_LEFT_FILE)));
            mTextures.put(ID_B4_LEFT, ImageUtils.loadImage(new File(path + "/" + B4_LEFT_FILE)));
            mTextures.put(ID_B5_LEFT, ImageUtils.loadImage(new File(path + "/" + B5_LEFT_FILE)));
            mTextures.put(ID_B6_LEFT, ImageUtils.loadImage(new File(path + "/" + B6_LEFT_FILE)));

            if (mTextures.size() < TEXTURE_COUNT) {
                throw new Exception("#Texturen < " + TEXTURE_COUNT);
            }

            for (Integer id : mTextures.keySet()) {
                Image current = mTextures.get(id);
                if ((current.getWidth(null) != iFieldWidth) || (current.getHeight(null) != iFieldHeight)) {
                    throw new Exception("Textur " + id + " hat nicht die erwartete Größe " + iFieldWidth + "x" + iFieldHeight);
                }
            }
        } catch (IOException ioe) {
            throw new Exception("Fehler beim laden des Grafikpaketes", ioe);
        } catch (Exception e) {
            throw new Exception("Grafikpaket ungültig (" + e.getMessage() + ")");
        }
    }

    public BufferedImage getOriginalSprite(int pID) {
        return mTextures.get(pID);
    }

    public BufferedImage getCachedImage(int pID, double pScaling) {
        try {
            HashMap<Double, BufferedImage> imageCache = cache.get(pID);
            if (imageCache == null) {
                imageCache = new HashMap<>();
                cache.put(pID, imageCache);
            }

            BufferedImage cached = imageCache.get(pScaling);
            if (cached == null) {
                Image scaled = mTextures.get(pID).getScaledInstance((int) (iFieldWidth / pScaling), (int) (iFieldHeight / pScaling), BufferedImage.SCALE_FAST);
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                GraphicsConfiguration gc = gd.getDefaultConfiguration();
                cached = gc.createCompatibleImage(scaled.getWidth(null), scaled.getHeight(null), Transparency.BITMASK);
                Graphics2D g = cached.createGraphics();
                ImageUtils.setupGraphics(g);
                g.drawImage(scaled, 0, 0, null);
                g.dispose();
                imageCache.put(pScaling, cached);
            }

            return cached;
        } catch (Exception e) {
            return null;
        }
    }

    public Image getImage(int pID, double pScaling) {
        try {
            HashMap<Double, BufferedImage> imageCache = cache.get(pID);
            if (imageCache == null) {
                imageCache = new HashMap<>();
                cache.put(pID, imageCache);
            }

            BufferedImage cached = imageCache.get(pScaling);
            if (cached == null) {
                Image scaled = mTextures.get(pID).getScaledInstance((int) (iFieldWidth / pScaling), (int) (iFieldHeight / pScaling), BufferedImage.SCALE_REPLICATE);

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

    public int getCurrentFieldWidth(double pZoom) {
        return (int) (iFieldWidth / pZoom);
    }

    public int getCurrentFieldHeight(double pZoom) {
        return (int) (iFieldHeight / pZoom);
    }

    public int getCurrentFieldWidth() {
        return (int) (iFieldWidth / MapPanel.getSingleton().getMapRenderer().getCurrentZoom());
    }

    public int getCurrentFieldHeight() {
        return (int) (iFieldHeight / MapPanel.getSingleton().getMapRenderer().getCurrentZoom());
    }

    public int getBasicFieldWidth() {
        return iFieldWidth;
    }

    public int getBasicFieldHeight() {
        return iFieldHeight;
    }
}
