/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.views.DSWorkbenchAttackFrame;
import de.tor.tribes.util.ProfileManager;
import de.tor.tribes.util.attack.AttackManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class UserProfile {

    private static Logger logger = Logger.getLogger("UserProfile");
    private static final int NO_UV = -1;
    private long iProfileId = -1;
    private String sServerId = null;
    private Properties mProperties = null;
    private Tribe tribe = null;

    public UserProfile() {
        mProperties = new Properties();
    }

    public void setTribeName(String pTribe) {
        addProperty("tribe.name", pTribe);
        tribe = null;
    }

    public String getTribeName() {
        return getProperty("tribe.name");
    }

    public Tribe getTribe() {
        if (tribe == null) {
            tribe = DataHolder.getSingleton().getTribeByName(getProperty("tribe.name"));
        }
        return tribe;
    }

    public void setProfileId(long pProfileId) {
        iProfileId = pProfileId;
    }

    public long getProfileId() {
        return iProfileId;
    }

    public void setServerId(String pServerId) {
        sServerId = pServerId;
    }

    public String getServerId() {
        return sServerId;
    }

    public void setUVId(int pId) {
        addProperty("uv.id", Integer.toString(pId));
    }

    public int getUVId() {
        try {
            return Integer.parseInt(getProperty("uv.id"));
        } catch (Exception e) {
            return NO_UV;
        }
    }

    public boolean isUVAccount() {
        return (getUVId() != NO_UV);
    }

    public void addProperty(String pKey, Object pValue) {
        mProperties.put(pKey, pValue);
    }

    public String getProperty(String pKey) {
        return mProperties.getProperty(pKey);
    }

    public String getProfileDirectory() {
        return "./servers/" + sServerId + "/profiles/" + getProfileId() + "/";
    }

    private void loadProperties(File pPropertiesPath) throws Exception {
        mProperties = new Properties();
        mProperties.load(new FileInputStream(pPropertiesPath));
    }

    public void updateProperties() {
        addProperty("last.x", DSWorkbenchMainFrame.getSingleton().getCurrentPosition()[0]);
        addProperty("last.y", DSWorkbenchMainFrame.getSingleton().getCurrentPosition()[1]);
        addProperty("zoom", Double.toString(DSWorkbenchMainFrame.getSingleton().getZoomFactor()));
    }

    public void restoreProperties() {
        try {
            int lastX = Integer.parseInt(getProperty("last.x"));
            int lastY = Integer.parseInt(getProperty("last.y"));
            DSWorkbenchMainFrame.getSingleton().centerPosition(lastX, lastY);
        } catch (Exception e) {
            logger.warn("Failed to set last map position. Probably this is a new UserProfile with no properties set");
        }
        try {
            double zoom = Double.parseDouble(getProperty("zoom"));
            DSWorkbenchMainFrame.getSingleton().setZoom(zoom);
        } catch (Exception e) {
            logger.warn("Failed to set last zoom factor. Probably this is a new UserProfile with no properties set");
        }

    }

    public boolean storeProfileData() {
        String profileDir = getProfileDirectory();
        if (!new File(profileDir).exists()) {
            if (!new File(profileDir).mkdirs()) {
                logger.error("Failed to create profile directory");
                return false;
            }
        }
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(getProfileDirectory() + "/profile.properties");
            mProperties.store(fout, "");
            fout.flush();
        } catch (Exception e) {
            logger.error("Failed to store profile properties");
            return false;
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                    fout = null;
                }
            } catch (IOException ex) {
            }
        }
        return true;
    }

    public static UserProfile loadProfile(String pServerId, long pProfileId) {
        UserProfile profile = new UserProfile();
        profile.setProfileId(pProfileId);
        profile.setServerId(pServerId);

        try {
            profile.loadProperties(new File(profile.getProfileDirectory() + "/profile.properties"));
            String uvIdProp = profile.getProperty("uv.id");
            if (uvIdProp != null) {
                profile.setUVId(Integer.parseInt(uvIdProp));
            } else {
                profile.setUVId(NO_UV);
            }
        } catch (Exception e) {
            logger.warn("Failed to load profile properties", e);
            return null;
        }
        return profile;
    }

    public static UserProfile create(String pServerId, String pTribe, int pUvId, boolean pReload) {
        if (pServerId == null || pTribe == null) {
            return null;
        }
        UserProfile profile = new UserProfile();
        profile.setProfileId(System.currentTimeMillis());
        profile.setServerId(pServerId);
        profile.addProperty("tribe.name", pTribe);
        profile.setUVId(pUvId);
        if (profile.storeProfileData()) {
            if (pReload) {
                ProfileManager.getSingleton().loadProfiles();
            }
            return profile;
        }
        return null;
    }

    public static UserProfile create(String pServerId, String pTribe) {
        return create(pServerId, pTribe, -1, true);
    }

    public static UserProfile createFast(String pServerId, String pTribe) {
        return create(pServerId, pTribe, -1, false);
    }

    public boolean delete() {
        boolean success = false;
        try {
            FileUtils.deleteDirectory(new File(getProfileDirectory()));
            success = true;
        } catch (IOException ioe) {

            try {
                FileUtils.touch(new File(getProfileDirectory() + File.separator + ".deleted"));
                FileUtils.forceDeleteOnExit(new File(getProfileDirectory()));
                success = true;
            } catch (IOException ioe2) {
            }
        }


        /*for (File f : new File(getProfileDirectory()).listFiles()) {
        if (!f.delete()) {
        success = false;
        break;
        }
        }
        if (success) {
        success = new File(getProfileDirectory()).delete();
        }*/
        ProfileManager.getSingleton().loadProfiles();
        return success;
    }

    @Override
    public String toString() {
        return getTribeName() + " (" + getServerId() + ")";
    }
}
