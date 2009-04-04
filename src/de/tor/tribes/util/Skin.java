/*
 * Skin.java
 *
 * Created on 13.09.2007, 17:08:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.SkinPreviewFrame;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import de.tor.tribes.ui.DSWorkbenchSettingsDialog;

/**
 * @author Charon
 */
public class Skin {

    private static Logger logger = Logger.getLogger("TexturePack");    //init with default skin dimensions
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
    private final String V1_FILE = "v1.png";
    private final String V2_FILE = "v2.png";
    private final String V3_FILE = "v3.png";
    private final String V4_FILE = "v4.png";
    private final String V5_FILE = "v5.png";
    private final String V6_FILE = "v6.png";
    private final String V1_LEFT_FILE = "v1_left.png";
    private final String V2_LEFT_FILE = "v2_left.png";
    private final String V3_LEFT_FILE = "v3_left.png";
    private final String V4_LEFT_FILE = "v4_left.png";
    private final String V5_LEFT_FILE = "v5_left.png";
    private final String V6_LEFT_FILE = "v6_left.png";
    private final String B1_FILE = "b1.png";
    private final String B2_FILE = "b2.png";
    private final String B3_FILE = "b3.png";
    private final String B4_FILE = "b4.png";
    private final String B5_FILE = "b5.png";
    private final String B6_FILE = "b6.png";
    private final String B1_LEFT_FILE = "b1_left.png";
    private final String B2_LEFT_FILE = "b2_left.png";
    private final String B3_LEFT_FILE = "b3_left.png";
    private final String B4_LEFT_FILE = "b4_left.png";
    private final String B5_LEFT_FILE = "b5_left.png";
    private final String B6_LEFT_FILE = "b6_left.png";
    private final String DEFAULT_UNDERGROUND = "default_underground.png";
    private final int TEXTURE_COUNT = 25;
    private static String BASE_PATH = "graphics/skins";
    private Hashtable<Integer, BufferedImage> mTextures = null;
    private Hashtable<Double, Hashtable<Integer, Image>> mCache = null;
    private String sSkinID = null;

    public Skin() throws Exception {
        loadSkin("default");
    }

    public Skin(String pSkinPath) throws Exception {
        if (pSkinPath == null || !pSkinPath.equals(MINIMAP_SKIN_ID)) {
            loadSkin(pSkinPath);
        } else {
            loadMinimapSkin();
        }
    }

    public boolean isMinimapSkin() {
        try {
            return (sSkinID.equals(MINIMAP_SKIN_ID));
        } catch (Exception e) {
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

    public static void showPreview(String pSkinID, Point pPos) throws Exception {
        try {
            SkinPreviewFrame f = new SkinPreviewFrame(new Skin(pSkinID));
            f.setLocation(pPos);
            f.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(DSWorkbenchSettingsDialog.getSingleton(), "Keine Vorschauf verfügbar.", "Informatione", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadMinimapSkin() throws Exception {
        sSkinID = MINIMAP_SKIN_ID;
        iFieldWidth = 10;
        iFieldHeight = 10;
        mTextures = new Hashtable<Integer, BufferedImage>();
    }

    private void loadSkin(String pSkinID) throws Exception {
        if (pSkinID == null) {
            pSkinID = "default";
        }

        sSkinID = pSkinID;
        String path = BASE_PATH + "/" + pSkinID;
        mTextures = new Hashtable<Integer, BufferedImage>();
        try {
            mTextures.put(ID_DEFAULT_UNDERGROUND, ImageIO.read(new File(path + "/" + DEFAULT_UNDERGROUND)));
            iFieldWidth = mTextures.get(0).getWidth(null);
            iFieldHeight = mTextures.get(0).getHeight(null);
            mTextures.put(ID_V1, ImageIO.read(new File(path + "/" + V1_FILE)));
            mTextures.put(ID_V2, ImageIO.read(new File(path + "/" + V2_FILE)));
            mTextures.put(ID_V3, ImageIO.read(new File(path + "/" + V3_FILE)));
            mTextures.put(ID_V4, ImageIO.read(new File(path + "/" + V4_FILE)));
            mTextures.put(ID_V5, ImageIO.read(new File(path + "/" + V5_FILE)));
            mTextures.put(ID_V6, ImageIO.read(new File(path + "/" + V6_FILE)));
            mTextures.put(ID_V1_LEFT, ImageIO.read(new File(path + "/" + V1_LEFT_FILE)));
            mTextures.put(ID_V2_LEFT, ImageIO.read(new File(path + "/" + V2_LEFT_FILE)));
            mTextures.put(ID_V3_LEFT, ImageIO.read(new File(path + "/" + V3_LEFT_FILE)));
            mTextures.put(ID_V4_LEFT, ImageIO.read(new File(path + "/" + V4_LEFT_FILE)));
            mTextures.put(ID_V5_LEFT, ImageIO.read(new File(path + "/" + V5_LEFT_FILE)));
            mTextures.put(ID_V6_LEFT, ImageIO.read(new File(path + "/" + V6_LEFT_FILE)));
            mTextures.put(ID_B1, ImageIO.read(new File(path + "/" + B1_FILE)));
            mTextures.put(ID_B2, ImageIO.read(new File(path + "/" + B2_FILE)));
            mTextures.put(ID_B3, ImageIO.read(new File(path + "/" + B3_FILE)));
            mTextures.put(ID_B4, ImageIO.read(new File(path + "/" + B4_FILE)));
            mTextures.put(ID_B5, ImageIO.read(new File(path + "/" + B5_FILE)));
            mTextures.put(ID_B6, ImageIO.read(new File(path + "/" + B6_FILE)));
            mTextures.put(ID_B1_LEFT, ImageIO.read(new File(path + "/" + B1_LEFT_FILE)));
            mTextures.put(ID_B2_LEFT, ImageIO.read(new File(path + "/" + B2_LEFT_FILE)));
            mTextures.put(ID_B3_LEFT, ImageIO.read(new File(path + "/" + B3_LEFT_FILE)));
            mTextures.put(ID_B4_LEFT, ImageIO.read(new File(path + "/" + B4_LEFT_FILE)));
            mTextures.put(ID_B5_LEFT, ImageIO.read(new File(path + "/" + B5_LEFT_FILE)));
            mTextures.put(ID_B6_LEFT, ImageIO.read(new File(path + "/" + B6_LEFT_FILE)));

            if (mTextures.size() < TEXTURE_COUNT) {
                throw new Exception("#Texturen < " + TEXTURE_COUNT);
            }

            Enumeration<Integer> imageIDs = mTextures.keys();
            while (imageIDs.hasMoreElements()) {
                Integer id = imageIDs.nextElement();
                Image current = mTextures.get(id);
                if ((current.getWidth(null) != iFieldWidth) || (current.getHeight(null) != iFieldHeight)) {
                    throw new Exception("Textur " + id + " hat nicht die erwartete Größe " + iFieldWidth + "x" + iFieldHeight);
                }

            }
        //  mCache = new Hashtable<Double, Hashtable<Integer, Image>>();
        //  mCache.put(1.0, mTextures);
        //try loading units, ignore exceptions due to not all skins have all units
        } catch (IOException ioe) {
            throw new Exception("Fehler beim laden des Grafikpaketes");
        } catch (Exception e) {
            throw new Exception("Grafikpaket ungültig (" + e.getMessage() + ")");
        }

    }

    public Image getImage(int pID, double pScaling) {
        if (pScaling == 1) {
            return mTextures.get(pID);
        }
        try {
            /*
            Hashtable<Integer, Image> cache = mCache.get(pScaling);
            if (cache != null) {
            if (cache.get(pID) == null) {
            cache.put(pID, mTextures.get(pID).getScaledInstance((int) (iFieldWidth / pScaling), (int) (iFieldHeight / pScaling), BufferedImage.SCALE_FAST));
            }

            } else {
            cache = new Hashtable<Integer, Image>();
            cache.put(pID, mTextures.get(pID).getScaledInstance((int) (iFieldWidth / pScaling), (int) (iFieldHeight / pScaling), BufferedImage.SCALE_FAST));
            mCache.put(pScaling, cache);
            }

            return cache.get(pID);*/
            Image ret = mTextures.get(pID).getScaledInstance((int) (iFieldWidth / pScaling), (int) (iFieldHeight / pScaling), BufferedImage.SCALE_FAST);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public int getCurrentFieldWidth() {
        return (int) (iFieldWidth / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
    }

    public int getCurrentFieldHeight() {
        return (int) (iFieldHeight / DSWorkbenchMainFrame.getSingleton().getZoomFactor());
    }

    public int getBasicFieldWidth() {
        return iFieldWidth;
    }

    public int getBasicFieldHeight() {
        return iFieldHeight;
    }

    public Point2D.Double getError() {
        double z = DSWorkbenchMainFrame.getSingleton().getZoomFactor();
        //get real size of one scaled texture
        double w = getBasicFieldWidth() / z;
        double h = getBasicFieldHeight() / z;
        //get int size of texture
        /*int ws = getImage(Skin.ID_DEFAULT_UNDERGROUND, z).getWidth(null);
        int hs = getImage(Skin.ID_DEFAULT_UNDERGROUND, z).getHeight(null);*/
        double ws = GlobalOptions.getSkin().getCurrentFieldWidth();
        double hs = GlobalOptions.getSkin().getCurrentFieldHeight();
        //calculate error in width and height
        double errorw = w / ws - 1;
        errorw *= (ws < w) ? - 1 : 1;
        double errorh = h / hs - 1;
        errorw *= (hs < h) ? - 1 : 1;
        return new Point2D.Double(errorw, errorh);
    }
}
