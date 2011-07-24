/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.UserProfile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class ProfileManager {

    private static Logger logger = Logger.getLogger("ProfileManager");
    private static ProfileManager SINGLETON = null;
    private List<UserProfile> mProfiles = null;
    private List<ProfileManagerListener> mListeners = null;

    public static synchronized ProfileManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ProfileManager();
        }
        return SINGLETON;
    }

    ProfileManager() {
        mProfiles = new LinkedList<UserProfile>();
        mListeners = new LinkedList<ProfileManagerListener>();
    }

    public void addProfileManagerListener(ProfileManagerListener pListener) {
        if (!mListeners.contains(pListener)) {
            mListeners.add(pListener);
        }
    }

    public void removeProfileManagerListener(ProfileManagerListener pListener) {
        mListeners.remove(pListener);
    }

    public UserProfile[] getProfiles() {
        Collections.sort(mProfiles, new Comparator<UserProfile>() {

            @Override
            public int compare(UserProfile o1, UserProfile o2) {
                if (o1.getServerId().length() < o2.getServerId().length()) {
                    return -1;
                } else if (o1.getServerId().length() > o2.getServerId().length()) {
                    return 1;
                }
                return o1.getServerId().compareTo(o2.getServerId());
            }
        });
        return mProfiles.toArray(new UserProfile[]{});
    }

    public UserProfile[] getProfiles(String pServer) {
        List<UserProfile> profilesForServer = new LinkedList<UserProfile>();
        for (UserProfile profile : mProfiles.toArray(new UserProfile[]{})) {
            if (profile.getServerId().equals(pServer)) {
                profilesForServer.add(profile);
            }
        }

        Collections.sort(profilesForServer, new Comparator<UserProfile>() {

            @Override
            public int compare(UserProfile o1, UserProfile o2) {
                if (o1.getServerId().length() < o2.getServerId().length()) {
                    return -1;
                } else if (o1.getServerId().length() > o2.getServerId().length()) {
                    return 1;
                }
                return o1.getServerId().compareTo(o2.getServerId());
            }
        });
        return profilesForServer.toArray(new UserProfile[]{});
    }

    public void loadProfiles() {
        mProfiles = new LinkedList<UserProfile>();
        File serversDir = new File("./servers/");
        for (File f : serversDir.listFiles()) {
            if (f.isDirectory()) {
                //server dir
                File profilesDir = new File(f.getPath() + "/profiles/");
                if (profilesDir.exists()) {
                    File[] profiles = profilesDir.listFiles();
                    if (profiles != null && profiles.length != 0) {
                        logger.debug("Got " + profiles.length + "profile directories");
                    }
                    for (File profileDir : profiles) {
                        logger.debug("Got profile directory '" + profileDir.getPath() + "'");
                        if (profileDir.isDirectory()) {
                            //profile directory
                            String profileId = profileDir.getName();
                            String serverName = f.getName();
                            logger.debug("Found profile #'" + profileId + "' on server '" + serverName + "'");
                            try {
                                UserProfile profile = UserProfile.loadProfile(serverName, Long.parseLong(profileId));
                                if (profile != null) {
                                    logger.info("Adding loaded profile #" + profileId);
                                    mProfiles.add(profile);
                                }
                            } catch (Exception e) {
                                logger.error("Failed to load profile", e);
                            }
                        }
                    }
                } else {
                    //handle old structure
                    logger.debug("Transforming legacy data structure to profile structure");
                    String server = f.getName();
                    Properties prop = new Properties();
                    try {
                        prop.load(new FileInputStream("global.properties"));

                        String player = prop.getProperty("player." + server);

                        logger.debug(" - found player '" + player + "' for server '" + server + "'");
                        UserProfile newProfile = UserProfile.createFast(server, player);

                        if (newProfile != null) {
                            mProfiles.add(newProfile);
                            //copy user data
                            File[] xmlFiles = f.listFiles(new FilenameFilter() {

                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.endsWith(".xml");
                                }
                            });
                            for (File xmlFile : xmlFiles) {
                                DataHolder.getSingleton().copyFile(xmlFile, new File(newProfile.getProfileDirectory() + "/" + xmlFile.getName()));
                            }
                        } else {
                            logger.error("Failed to transform profile");
                        }
                    } catch (Exception e) {
                    }
                }
            }

        }
        fireProfilesLoadedEvents();
    }

    public void fireProfilesLoadedEvents() {
        ProfileManagerListener[] listeners = mListeners.toArray(new ProfileManagerListener[mListeners.size()]);
        for (ProfileManagerListener listener : listeners) {
            listener.fireProfilesLoadedEvent();
        }
    }

    public static void main(String[] args) {
        ProfileManager.getSingleton().loadProfiles();
    }
}
