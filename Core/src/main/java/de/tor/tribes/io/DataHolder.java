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

import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.*;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalDefaults;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.xml.JaxenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author Charon
 */
public class DataHolder {
    private static Logger logger = Logger.getLogger("DataManager");
    private static final int ID_OFF = 0;
    private static final int ID_DEF = 1;
    private Village[][] mVillages = null;
    private Hashtable<Integer, Village> mVillagesTable = null;
    private Hashtable<Integer, Ally> mAllies = null;
    private Hashtable<Integer, Tribe> mTribes = null;
    private Hashtable<String, Ally> mAlliesByName = null;
    private Hashtable<String, Ally> mAlliesByTagName = null;
    private Hashtable<String, Tribe> mTribesByName = null;
    private List<UnitHolder> mUnits = null;
    private Hashtable<String, UnitHolder> mUnitsByName = null;
    private List<DataHolderListener> mListeners = null;
    private boolean bAborted = false;
    private static DataHolder SINGLETON = null;
    private boolean loading = false;
    private int currentBonusType = 0;
    private boolean DATA_VALID = false;

    public static synchronized DataHolder getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DataHolder();
        }
        return SINGLETON;
    }

    DataHolder() {
        mListeners = new LinkedList<>();
        initialize();
    }

    /**
     * Clear all data an re-initialize the structures
     */
    private void initialize() {
        mVillages = null;
        mVillagesTable = null;
        mAllies = null;
        mTribes = null;
        mUnits = null;
        removeTempData();

        mVillages = new Village[1000][1000];
        mVillagesTable = new Hashtable<>();
        mAllies = new Hashtable<>();
        mTribes = new Hashtable<>();
        mTribesByName = new Hashtable<>();
        mAlliesByName = new Hashtable<>();
        mAlliesByTagName = new Hashtable<>();
        mUnitsByName = new Hashtable<>();
        mUnits = new LinkedList<>();
        DATA_VALID = false;
    }

    public boolean isDataValid() {
        return DATA_VALID;
    }

    public long getDataAge() {
        File villageFile = new File(getDataDirectory() + "/" + "village.txt.gz");
        File tribeFile = new File(getDataDirectory() + "/" + "tribe.txt.gz");
        File allyFile = new File(getDataDirectory() + "/" + "ally.txt.gz");
        return Math.max(allyFile.lastModified(), Math.min(villageFile.lastModified(), tribeFile.lastModified()));
    }

    public synchronized void addDataHolderListener(DataHolderListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mListeners.contains(pListener)) {
            mListeners.add(pListener);
        }
    }

    public synchronized void removeDataHolderListener(DataHolderListener pListener) {
        mListeners.remove(pListener);
    }

    /**
     * Get the server data directory, depending on the selected server
     */
    public String getDataDirectory() {
        return Constants.SERVER_DIR + "/" + GlobalOptions.getSelectedServer();
    }

    public int getCurrentBonusType() {
        return currentBonusType;
    }

    public boolean isDataAvailable(String pServerId) {
        String dataDir = Constants.SERVER_DIR + "/" + pServerId;
        if (pServerId == null) {
            dataDir = getDataDirectory();
        }
        File villageFile = new File(dataDir + "/" + "village.txt.gz");
        File tribeFile = new File(dataDir + "/" + "tribe.txt.gz");
        File allyFile = new File(dataDir + "/" + "ally.txt.gz");
        File units = new File(dataDir + "/" + "units.xml");
        File buildings = new File(dataDir + "/" + "buildings.xml");
        File settings = new File(dataDir + "/" + "settings.xml");

        return (villageFile.exists() && tribeFile.exists() && allyFile.exists() && units.exists() && buildings.exists() && settings.exists());
    }

    public boolean isDataAvailable() {
        return isDataAvailable(null);
    }

    /**
     * Check if server is supported or not.
     */
    public boolean serverSupported() {
        fireDataHolderEvents("Prüfe Server Einstellungen");
        File settings = new File(getDataDirectory() + "/settings.xml");
        try {
            if (settings.exists()) {
                ServerSettings.getSingleton().loadSettings(GlobalOptions.getSelectedServer());
                try {
                    currentBonusType = ServerSettings.getSingleton().getNewBonus();
                } catch (Exception e) {
                    //bonus_new field not found. Set to old type
                    currentBonusType = 0;
                }
            } else {
                if (GlobalOptions.isOfflineMode()) {
                    fireDataHolderEvents("Servereinstellungen nicht gefunden. Download im Offline-Modus nicht möglich.");
                    return false;
                } else {
                    //download settings.xml
                    logger.debug("Server settings do not exist at " + getDataDirectory());
                    String sURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
                    new File(DataHolder.getSingleton().getDataDirectory()).mkdirs();
                    fireDataHolderEvents("Lese Server Einstellungen");
                    URL file = new URL(sURL + "/interface.php?func=get_config");
                    logger.debug("Try downloading server settings from " + sURL + "/interface.php?func=get_config");
                    downloadDataFile(file, "settings_tmp.xml");
                    copyFile(new File("settings_tmp.xml"), settings);

                    if (!ServerSettings.getSingleton().loadSettings(GlobalOptions.getSelectedServer())) {
                        throw new Exception("Failed to load server settings");
                    }

                    try {
                        currentBonusType = ServerSettings.getSingleton().getNewBonus();
                    } catch (Exception e) {
                        //bonus_new field not found. Set to old type
                        currentBonusType = 0;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check server settings", e);
            settings.delete();
            return false;
        }
        return true;
    }

    /**
     * Update the data, optionally by downloading
     *
     * @param pReload
     * @return
     */
    public boolean loadData(boolean pReload) {
        loading = true;
        initialize();
        try {
            String serverID = GlobalOptions.getSelectedServer();
            String sURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());

            logger.info("Calling 'loadData()' for server " + serverID);
            try {
                boolean invalidServer = false;
                if (sURL == null) {
                    logger.warn("No server URL returned for selected server " + GlobalOptions.getSelectedServer() + ". Returning 'false'.");
                    fireDataHolderEvents("Server nicht verfügbar. Welddatendownload nicht möglich.");
                    invalidServer = true;
                    pReload = false;
                }

                boolean recreateLocal;
                if (invalidServer || serverSupported()) {
                    if (pReload && !GlobalOptions.isOfflineMode()) {
                        fireDataHolderEvents("Download der aktuellen Weltdaten gestartet");
                        logger.debug(" - Initiating full reload");
                        if (downloadData()) {
                            logger.debug(" - Download succeeded");
                            fireDataHolderEvents("Download erfolgreich");
                            recreateLocal = true;
                        } else {
                            logger.error(" - Download failed");
                            fireDataHolderEvents("Download fehlgeschlagen. Versuche lokale Kopie zu laden");
                            if (isDataAvailable()) {
                                logger.debug(" - local data is available, try to load it");
                                if (readLocalDataCopy(getDataDirectory())) {
                                    logger.debug(" - Local copy successfully read");
                                    fireDataHolderEvents("Lokale Kopie erfolgreich geladen");
                                    recreateLocal = false;
                                } else {
                                    logger.error(" - Reading local copy failed");
                                    fireDataHolderEvents("Lokale Kopie fehlerhaft. Laden wird abgebrochen!");
                                    loading = false;
                                    fireDataLoadedEvents(false);
                                    GlobalOptions.setInternalDataDamaged(true);
                                    return false;
                                }
                            } else {
                                fireDataHolderEvents("Lokale Kopie nicht vorhanden. Versuche erneuten Download");
                                if (downloadData()) {
                                    logger.debug(" - Download succeeded");
                                    fireDataHolderEvents("Download erfolgreich.");
                                    recreateLocal = true;
                                } else {
                                    logger.error(" - Second try failed");
                                    fireDataHolderEvents("Erneuter Downloadversuch fehlgeschlagen. Laden wird abgebrochen!");
                                    loading = false;
                                    fireDataLoadedEvents(false);
                                    GlobalOptions.setInternalDataDamaged(true);
                                    return false;
                                }
                            }
                        }//end of download failed branch
                    } else {//end of download branch
                        fireDataHolderEvents("Lesen der existierenden Weltdaten gestartet");
                        if (isDataAvailable()) {
                            logger.debug(" - local data is available, try to load it");
                            if (readLocalDataCopy(getDataDirectory())) {
                                logger.debug(" - Local copy successfully read");
                                fireDataHolderEvents("Lokale Kopie erfolgreich geladen");
                                recreateLocal = false;
                            } else {
                                logger.error(" - Reading local copy failed");
                                fireDataHolderEvents("Lokale Kopie fehlerhaft. Laden wird abgebrochen!");
                                loading = false;
                                fireDataLoadedEvents(false);
                                GlobalOptions.setInternalDataDamaged(true);
                                return false;
                            }
                        } else {//end of reading available data
                            fireDataHolderEvents("Lokale Kopie nicht vorhanden. Versuche erneuten Download");
                            if (!invalidServer && downloadData()) {
                                logger.debug(" - Download succeeded");
                                fireDataHolderEvents("Download erfolgreich.");
                                recreateLocal = true;
                            } else {
                                logger.error(" - Second try failed");
                                fireDataHolderEvents("Erneuter Downloadversuch fehlgeschlagen. Laden wird abgebrochen!");
                                loading = false;
                                fireDataLoadedEvents(false);
                                GlobalOptions.setInternalDataDamaged(true);
                                return false;
                            }
                        }
                    }//end of read local data
                } else {
                    logger.error("Failed to read server settings");
                    fireDataHolderEvents("Fehler beim Lesen der Servereinstellungen. Möglicherweise ist der gewählte Server gerade offline. Versuch es in wenigen Minuten noch einmal.");
                    fireDataLoadedEvents(false);
                    GlobalOptions.setInternalDataDamaged(true);
                    return false;
                }

                if (!GlobalOptions.isOfflineMode()) {
                    fireDataHolderEvents("Prüfe Dekoration");
                    try {
                        WorldDecorationHolder.initialize();
                    } catch (Exception e) {
                        logger.error("Failed to read world decoration", e);
                    }
                }

                //setting internal data to be valid
                GlobalOptions.setInternalDataDamaged(false);

                //parse additional data
                logger.info("Reading conquered units");
                BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_att.txt.gz"))));
                String line = "";
                while ((line = r.readLine()) != null) {
                    try {
                        parseConqueredLine(line, ID_OFF);
                    } catch (Exception e) {
                        //ignored (should only occur on single lines)
                    }
                }
                r.close();

                r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_def.txt.gz"))));
                line = "";
                while ((line = r.readLine()) != null) {
                    try {
                        parseConqueredLine(line, ID_DEF);
                    } catch (Exception e) {
                        //ignored (should only occur on single lines)
                    }
                }
                r.close();
                //do post processing
                fireDataHolderEvents("Kombiniere Daten...");
                mergeData();
                fireDataHolderEvents("Lese Servereinstellungen...");
                parseUnits();
                fireDataHolderEvents("Daten erfolgreich gelesen");
                if (!isDataAvailable() || recreateLocal) {
                    fireDataHolderEvents("Erstelle lokale Kopie");
                    if (createLocalDataCopy(getDataDirectory())) {
                        fireDataHolderEvents("Daten erfolgreich geladen");
                    } else {
                        fireDataHolderEvents("Fehler beim Erstellen der lokale Kopie");
                    }
                }
                logger.info("Loading finished");
            } catch (Exception e) {
                fireDataHolderEvents("Fehler beim Lesen der Daten.");
                logger.error("Failed to read server data", e);
                if (bAborted) {
                    loading = false;
                    fireDataLoadedEvents(false);
                    return false;
                }
            }
            loading = false;
            fireDataLoadedEvents(true);
        } catch (Exception e) {
            logger.error("Global exception while loading data", e);
            loading = false;
            fireDataLoadedEvents(false);
            return false;
        }

        //reinitialise defaults because of changed units
        GlobalDefaults.reinit();
        return true;
    }

    public boolean loadLiveData() {
        loading = true;
        initialize();
        try {
            String serverID = GlobalOptions.getSelectedServer();
            logger.info("Calling 'loadLiveData()' for server " + serverID);
            try {
                boolean recreateLocal = false;
                if (serverSupported()) {
                    if (!GlobalOptions.isOfflineMode()) {
                        fireDataHolderEvents("Download der aktuellen Weltdaten von die-staemme.de gestartet");
                        logger.debug(" - Initiating full reload of live data");
                        if (downloadData()) {
                            logger.debug(" - Download succeeded");
                            fireDataHolderEvents("Download erfolgreich");
                            recreateLocal = true;
                        } else {
                            logger.error(" - Download failed");
                            fireDataHolderEvents("Download von die-staemme.de fehlgeschlagen. Versuche lokale Kopie zu laden");
                            if (isDataAvailable()) {
                                logger.debug(" - local data is available, try to load it");
                                if (readLocalDataCopy(getDataDirectory())) {
                                    logger.debug(" - Local copy successfully read");
                                    fireDataHolderEvents("Lokale Kopie erfolgreich geladen");
                                    recreateLocal = false;
                                } else {
                                    logger.error(" - Reading local copy failed");
                                    fireDataHolderEvents("Lokale Kopie fehlerhaft. Laden wird abgebrochen!");
                                    loading = false;
                                    fireDataLoadedEvents(false);
                                    GlobalOptions.setInternalDataDamaged(true);
                                    return false;
                                }
                            } else {
                                fireDataHolderEvents("Lokale Kopie nicht vorhanden. Versuche Download vom DS Workbench Server");
                                if (downloadData()) {
                                    logger.debug(" - Download succeeded");
                                    fireDataHolderEvents("Download erfolgreich.");
                                    recreateLocal = true;
                                } else {
                                    logger.error(" - Second try failed");
                                    fireDataHolderEvents("Downloadversuch vom DS Workbench Server fehlgeschlagen. Download wird abgebrochen!");
                                    loading = false;
                                    fireDataLoadedEvents(false);
                                    GlobalOptions.setInternalDataDamaged(true);
                                    return false;
                                }
                            }
                        }//end of download failed branch
                    } else {//end of download branch
                        fireDataHolderEvents("Lesen der existierenden Weltdaten gestartet");
                        if (isDataAvailable()) {
                            logger.debug(" - local data is available, try to load it");
                            if (readLocalDataCopy(getDataDirectory())) {
                                logger.debug(" - Local copy successfully read");
                                fireDataHolderEvents("Lokale Kopie erfolgreich geladen");
                                recreateLocal = false;
                            } else {
                                logger.error(" - Reading local copy failed");
                                fireDataHolderEvents("Lokale Kopie fehlerhaft. Laden wird abgebrochen!");
                                loading = false;
                                fireDataLoadedEvents(false);
                                GlobalOptions.setInternalDataDamaged(true);
                                return false;
                            }
                        } else {//end of reading available data
                            fireDataHolderEvents("Lokale Kopie nicht vorhanden. Versuche erneuten Download");
                            if (downloadData()) {
                                logger.debug(" - Download succeeded");
                                fireDataHolderEvents("Download erfolgreich.");
                                recreateLocal = true;
                            } else {
                                logger.error(" - Second try failed");
                                fireDataHolderEvents("Erneuter Downloadversuch fehlgeschlagen. Download wird abgebrochen!");
                                loading = false;
                                fireDataLoadedEvents(false);
                                GlobalOptions.setInternalDataDamaged(true);
                                return false;
                            }
                        }
                    }//end of read local data
                } else {
                    logger.error("Failed to read server settings");
                    fireDataHolderEvents("Fehler beim Lesen der Servereinstellungen. Möglicherweise ist der gewählte Server gerade offline. Versuch es in wenigen Minuten noch einmal.");
                    fireDataLoadedEvents(false);
                    GlobalOptions.setInternalDataDamaged(true);
                    return false;
                }

                //setting internal data to be valid
                GlobalOptions.setInternalDataDamaged(false);

                //parse additional data
                logger.info("Reading conquered units");
                BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_att.txt.gz"))));
                String line = "";
                while ((line = r.readLine()) != null) {
                    try {
                        parseConqueredLine(line, ID_OFF);
                    } catch (Exception e) {
                        //ignored (should only occur on single lines)
                    }
                }
                r.close();

                r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_def.txt.gz"))));
                line = "";
                while ((line = r.readLine()) != null) {
                    try {
                        parseConqueredLine(line, ID_DEF);
                    } catch (Exception e) {
                        //ignored (should only occur on single lines)
                    }
                }
                r.close();
                //do post processing
                fireDataHolderEvents("Kombiniere Daten...");
                mergeData();

                fireDataHolderEvents("Lese Servereinstellungen...");
                parseUnits();
                fireDataHolderEvents("Daten erfolgreich gelesen");
                if (!isDataAvailable() || recreateLocal) {
                    fireDataHolderEvents("Erstelle lokale Kopie");
                    if (createLocalDataCopy(getDataDirectory())) {
                        fireDataHolderEvents("Daten erfolgreich geladen");
                    } else {
                        fireDataHolderEvents("Fehler beim Erstellen der lokale Kopie");
                    }
                }
                logger.info("Loading finished");
            } catch (Exception e) {
                fireDataHolderEvents("Fehler beim Lesen der Daten.");
                logger.error("Failed to read server data", e);
                if (bAborted) {
                    loading = false;
                    fireDataLoadedEvents(false);
                    return false;
                }
            }
            loading = false;
            fireDataLoadedEvents(true);
        } catch (Exception e) {
            logger.error("Global exception while loading data", e);
            loading = false;
            fireDataLoadedEvents(false);
            return false;
        }
        return true;
    }

    public boolean isLoading() {
        return loading;
    }

    private boolean createLocalDataCopy(String pServerDir) {
        try {
            copyFile(new File("./village.tmp"), new File(pServerDir + "/village.txt.gz"));
            copyFile(new File("./tribe.tmp"), new File(pServerDir + "/tribe.txt.gz"));
            copyFile(new File("./ally.tmp"), new File(pServerDir + "/ally.txt.gz"));
            return true;
        } catch (Exception e) {
            logger.error("Failed to create local data copy", e);
            return false;
        }

    }

    public boolean readLocalDataCopy(String pServerDir) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pServerDir + "/village.txt.gz"))));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Village v = Village.parseFromPlainData(line);
                try {
                    if(v != null) {
                        mVillages[v.getX()][v.getY()] = v;
                    }
                } catch (Exception e) {
                    //ignore invalid village
                }
            }
            r.close();
            getTribesForServer(GlobalOptions.getSelectedServer(), mTribes);

            Collection<Tribe> tribes = mTribes.values();
            for (Tribe t : tribes) {
                if (t != null && t.getName() != null) {
                    mTribesByName.put(t.getName(), t);
                }
            }

            getAlliesForServer(pServerDir, mAllies);

            Collection<Ally> allies = mAllies.values();
            for (Ally a : allies) {
                if (a != null && a.getName() != null && a.getTag() != null) {
                    mAlliesByName.put(a.getName(), a);
                    mAlliesByTagName.put(a.getTag(), a);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to read local data copy", e);
            return false;
        }
        return true;
    }

    public Hashtable<Integer, Tribe> getTribesForServer(String pServer) {
        return getTribesForServer(pServer, null);
    }

    private Hashtable<Integer, Tribe> getTribesForServer(String pServer, Hashtable<Integer, Tribe> pTribes) {
        try {
            String dataDir = Constants.SERVER_DIR + "/" + pServer;
            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataDir + "/tribe.txt.gz"))));
            if (pTribes == null) {
                pTribes = new Hashtable<>();
            }
            String line;

            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Tribe t = Tribe.parseFromPlainData(line);
                if (t != null) {
                    pTribes.put(t.getId(), t);
                }
            }

            r.close();
        } catch (Exception e) {
            logger.error("Failed to read tribes for server '" + pServer + "'", e);
        }
        return pTribes;
    }

    public Hashtable<Integer, Ally> getAlliesForServer(String pServer) {
        return getAlliesForServer(pServer, null);
    }

    private Hashtable<Integer, Ally> getAlliesForServer(String pServer, Hashtable<Integer, Ally> pAllies) {
        try {
            String dataDir = Constants.SERVER_DIR + "/" + GlobalOptions.getSelectedServer();
            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataDir + "/ally.txt.gz"))));
            if (pAllies == null) {
                pAllies = new Hashtable<>();
            }
            String line;

            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Ally a = Ally.parseFromPlainData(line);
                if (a != null) {
                    pAllies.put(a.getId(), a);
                }
            }

            r.close();
        } catch (Exception e) {
            logger.error("Failed to read allies for server '" + pServer + "'");
        }
        return pAllies;
    }

    /**
     * Download all needed data files (villages, tribes, allies, kills,
     * settings)
     */
    private boolean downloadData() {
        URL file = null;
        String serverDir = getDataDirectory();
        logger.info("Using server dir '" + serverDir + "'");
        new File(serverDir).mkdirs();

        try {

            // <editor-fold defaultstate="collapsed" desc="Server settings check">
            //download settings.xml
            String sURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            logger.debug("Download server settings");
            fireDataHolderEvents("Lese Server Einstellungen");
            File target = new File(serverDir + "/settings.xml");
            if (target.exists()) {
                target.delete();
            }

            file = new URL(sURL + "/interface.php?func=get_config");
            downloadDataFile(file, "settings_tmp.xml");
            //new File("settings_tmp.xml").renameTo(target);
            copyFile(new File("settings_tmp.xml"), target);
            if (!serverSupported()) {
                return false;
            }
            //</editor-fold>

            //load villages
            logger.info("Downloading new data version from " + sURL);
            //clear all data structures
            //initialize();

            // <editor-fold defaultstate="collapsed" desc=" Load villages ">
            fireDataHolderEvents("Lade Dörferliste");
            file = new URL(sURL + "/map/village.txt.gz");

            logger.debug(" + Start reading villages");
            downloadDataFile(file, "village.tmp");
            logger.debug(" - Finished reading villages");

            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("village.tmp"))));
            String line = "";
            logger.debug(" + Start parsing villages");

            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Village v = Village.parseFromPlainData(line);
                if (v != null && v.getX() >= 0 && v.getX() < mVillages.length && v.getY() >= 0 && v.getY() < mVillages[0].length) {
                    mVillages[v.getX()][v.getY()] = v;
                }
            }
            r.close();
            logger.debug(" - Finished parsing villages");
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Load tribes ">
            fireDataHolderEvents("Lade Spielerliste");

            file = new URL(sURL + "/map/tribe.txt.gz");
            logger.debug(" + Start reading tribes");
            downloadDataFile(file, "tribe.tmp");
            logger.debug(" - Finished reading tribes");

            r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("tribe.tmp"))));

            line = "";
            logger.debug(" + Start parsing tribes");
            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Tribe t = Tribe.parseFromPlainData(line);
                if (t != null && t.getName() != null) {
                    mTribes.put(t.getId(), t);
                    mTribesByName.put(t.getName(), t);
                }
            }
            r.close();
            logger.debug(" - Finished parsing tribes");
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Load allies ">
            fireDataHolderEvents("Lade Stämmeliste");
            file = new URL(sURL + "/map/ally.txt.gz");
            logger.debug(" + Start reading allies");
            downloadDataFile(file, "ally.tmp");
            logger.debug(" - Finished reading allies");

            r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("ally.tmp"))));
            line = "";
            logger.debug(" + Start parsing allies");
            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Ally a = Ally.parseFromPlainData(line);
                if (a != null && a.getName() != null && a.getTag() != null) {
                    mAllies.put(a.getId(), a);
                    mAlliesByName.put(a.getName(), a);
                    mAlliesByTagName.put(a.getTag(), a);
                }
            }
            logger.debug(" - Finished parsing allies");
            r.close();

            // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc=" Load conquers off ">
            fireDataHolderEvents("Lese besiegte Gegner (Angriff)...");
            target = new File(serverDir + "/kill_att.txt.gz");
            file = new URL(sURL + "/map/kill_att.txt.gz");
            logger.debug(" + Downloading conquers (off)");
            downloadDataFile(file, "kill_att.tmp");
            if (target.exists()) {
                target.delete();
            }
            //  new File("kill_att.tmp").renameTo(target);
            copyFile(new File("kill_att.tmp"), target);
            logger.debug(" - Finished downloading conquers (off)");
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" Load conquers def ">
            fireDataHolderEvents("Lese besiegte Gegner (Verteidigung)...");
            target = new File(serverDir + "/kill_def.txt.gz");
            file = new URL(sURL + "/map/kill_def.txt.gz");
            logger.debug(" + Downloading conquers (def)");
            downloadDataFile(file, "kill_def.tmp");
            if (target.exists()) {
                target.delete();
            }
            //   new File("kill_def.tmp").renameTo(target);
            copyFile(new File("kill_def.tmp"), target);
            logger.debug(" - Finished downloading conquers (def)");
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Direct download from DS-Servers">
            //download unit information, but only once
            target = new File(serverDir + "/units.xml");
            if (!target.exists()) {
                logger.debug("Loading unit config file from server");
                fireDataHolderEvents("Lade Information über Einheiten");
                file = new URL(sURL + "/interface.php?func=get_unit_info");
                downloadDataFile(file, "units_tmp.xml");

                //new File("units_tmp.xml").renameTo(target);
                copyFile(new File("units_tmp.xml"), target);
            }

            //download building information, but only once
            target = new File(serverDir + "/buildings.xml");
            if (!target.exists()) {
                logger.debug("Loading building config file from server");
                fireDataHolderEvents("Lade Information über Gebäude");
                file = new URL(sURL + "/interface.php?func=get_building_info");
                downloadDataFile(file, "buildings_tmp.xml");
                // new File("buildings_tmp.xml").renameTo(target);
                copyFile(new File("buildings_tmp.xml"), target);
            }
            //</editor-fold>

            // DatabaseInterface.updateDataVersion(accountName, serverID);
            fireDataHolderEvents("Download von die-staemme.de erfolgreich beendet.");
        } catch (Throwable t) {
            fireDataHolderEvents("Download von die-staemme.de fehlgeschlagen.");
            logger.error("Failed to download live data", t);
            return false;
        }
        return true;
    }

    /**
     * Merge all data into the village data structure to ease searching
     */
    private void mergeData() {
        for (Village[] mVillage : mVillages) {
            for (int j = 0; j < mVillages[0].length; j++) {
                Village current = mVillage[j];
                if (current != null) {
                    //set tribe of village
                    Tribe t = mTribes.get(current.getTribeID());
                    //set tribe of village
                    current.setTribe(t);
                    if (t != null) {
                        //add village to tribe
                        t.addVillage(current);
                        if (t.getAlly() == null) {
                            Ally currentAlly = mAllies.get(t.getAllyID());
                            //set ally of tribe
                            t.setAlly(currentAlly);
                            if (currentAlly != null) {
                                //add tribe to ally
                                currentAlly.addTribe(t);
                            }
                        }
                    }
                    mVillagesTable.put(current.getId(), current);
                }
            }
        }

        logger.debug("Removing empty allies");
        Enumeration<Integer> allyKeys = mAllies.keys();
        List<Ally> toRemove = new LinkedList<>();
        while (allyKeys.hasMoreElements()) {
            Ally a = mAllies.get(allyKeys.nextElement());
            if (a.getTribes() == null || a.getTribes().length == 0) {
                toRemove.add(a);
            }
        }
        for (Ally a : toRemove) {
            mAllies.remove(a.getId());
            mAlliesByName.remove(a.getName());
            mAlliesByTagName.remove(a.getTag());
        }

        logger.debug("Updating tribes with no allies");
        Enumeration<Integer> tribeKeys = mTribes.keys();
        NoAlly.getSingleton().reset();
        while (tribeKeys.hasMoreElements()) {
            Tribe t = mTribes.get(tribeKeys.nextElement());
            if (t.getAllyID() == 0) {
                NoAlly.getSingleton().addTribe(t);
            }
        }

        logger.debug("Removed " + toRemove.size() + " empty allies");
        DATA_VALID = true;
    }

    /**
     * Download one single file from a URL
     */
    private void downloadDataFile(URL pSource, String pLocalName) throws Exception {
        URLConnection ucon = pSource.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
        FileOutputStream tempWriter = new FileOutputStream(pLocalName);
        InputStream isr = ucon.getInputStream();
        int bytes = 0;
        byte[] data = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while ((bytes = isr.read(data)) != -1) {
            result.write(data, 0, bytes);
        }

        tempWriter.write(result.toByteArray());
        tempWriter.flush();
        try {
            isr.close();
        } catch (Exception ignored) {
        }
        try {
            tempWriter.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Parse a line of a conquered units file and set the data for the
     * associated tribe
     */
    private void parseConqueredLine(String pLine, int pType) {
        StringTokenizer tokenizer = new StringTokenizer(pLine, ",");
        try {
            String rank = tokenizer.nextToken();
            String tribeID = tokenizer.nextToken();
            String kills = tokenizer.nextToken();
            Tribe t = mTribes.get(Integer.parseInt(tribeID));
            if (pType == ID_OFF) {
                t.setKillsAtt(Double.parseDouble(kills));
                t.setRankAtt(Integer.parseInt(rank));
            } else {
                t.setKillsDef(Double.parseDouble(kills));
                t.setRankDeff(Integer.parseInt(rank));
            }
        } catch (Exception e) {
            //sth went wrong with the current kill entry, ignore it
        }
    }

    /**
     * Parse the list of units
     */
    private void parseUnits() {
        mUnits.clear();
        mUnitsByName.clear();
        String unitFile = getDataDirectory() + "/units.xml";
        //buildingsFile += "/units.xml";
        logger.debug("Loading units");
        try {
            Document d = JaxenUtils.getDocument(new File(unitFile));
            List<Element> l = (List<Element>) JaxenUtils.getNodes(d, "/config/*");
            for (Element e : l) {
                try {
                    UnitHolder unit = new UnitHolder(e);
                    if (unit.getPlainName() != null) {
                        mUnits.add(unit);
                        mUnitsByName.put(unit.getPlainName(), unit);
                    }
                } catch (Exception inner) {
                    logger.error("Failed loading unit", inner);
                }
            }
        } catch (Exception outer) {
            logger.error("Failed to load units", outer);
            fireDataHolderEvents("Laden der Einheiten fehlgeschlagen");
        }
    }

    public void copyFile(File pSource, File pDestination) {
        try {
            FileUtils.copyFile(pSource, pDestination);
        } catch (IOException ioe) {
            logger.error("Failed to copy file '" + pSource.getPath() + "' to '" + pDestination.getPath() + "'");
        }
    }

    /**
     * Get all villages<BR> !!Attention!!<B>This call blocks while loading data.
     * It is only intended to be used internally</B> !!Attention!!
     */
    public synchronized Village[][] getVillages() {
        if (loading) {
            //block getting villages while loading to avoid nullpointer exceptions
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        return mVillages;
    }

    public void removeTempData() {
        System.gc();
        System.gc();
    }

    /**
     * Should not be used to often because it is not optimized therefor
     * @return random Village with owner
     */
    public Village getRandomVillageWithOwner() {
        List<Tribe> tribeList = new ArrayList<>(mTribes.values());
        Collections.shuffle(tribeList);
        
        for(Tribe t: tribeList) {
            if(t.getVillages() > 0) {
                int rnd = (int) (Math.random() * t.getVillages());
                return t.getVillageList()[rnd];
            }
        }
        return null;
    }

    public Village getRandomVillage() {
        try {
            List<Village> villageList = new ArrayList<>(mVillagesTable.values());
            return villageList.get((int) (Math.random() * villageList.size()));
        } catch (Exception e) {
            return null;
        }
    }

    public int countVisibleVillages(Point pStart, Point pEnd) {
        int cnt = 0;
        //sort coordinates
        int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
        int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
        int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
        int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                try {
                    Village v = DataHolder.getSingleton().getVillages()[x][y];
                    if (v != null && v.isVisibleOnMap()) {
                        cnt++;
                    }
                } catch (Exception e) {
                    //avoid IndexOutOfBounds if selection is too small
                }
            }
        }
        return cnt;
    }

    public List<Village> getVillagesInRegion(Point pStart, Point pEnd) {
        List<Village> marked = new ArrayList<>();
        try {
            int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
            int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
            int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
            int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
            boolean showBarbarian = GlobalOptions.getProperties().getBoolean("show.barbarian");
            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    Village v = getVillages()[x][y];
                    if ((v != null && v.getTribe() == Barbarians.getSingleton()) && !showBarbarian) {
                        //dont select barbarians if they are not visible
                    } else {
                        if (v != null && !marked.contains(v)) {
                            marked.add(v);
                        }
                    }
                }
            }
            //Collections.sort(marked, Village.ALLY_TRIBE_VILLAGE_COMPARATOR);
        } catch (Exception e) {
            //occurs if no rect was opened by selection tool -> ignore
        }
        return marked;
    }

    /**
     * Get villages as a hashtable ordered by IDs
     */
    public synchronized Hashtable<Integer, Village> getVillagesById() {
        if (loading) {
            //block getting villages while loading to avoid nullpointer exceptions
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        return mVillagesTable;
    }

    /**
     * Get all allies
     */
    public Hashtable<Integer, Ally> getAllies() {
        return mAllies;
    }

    /**
     * Search the ally list for the ally with the provided name
     */
    public Ally getAllyByName(String pName) {
        Ally result = null;
        if (pName != null) {
            result = mAlliesByName.get(pName);
        }
        return result;
    }

    /**
     * Search the ally list for the ally with the provided tag name
     */
    public Ally getAllyByTagName(String pTagName) {
        Ally result = null;
        if (pTagName != null) {
            result = mAlliesByTagName.get(pTagName);
        }
        return result;
    }

    /**
     * Get all tribes
     */
    public Hashtable<Integer, Tribe> getTribes() {
        return mTribes;
    }

    /**
     * Search the tribes list for the tribe with the provided name
     */
    public Tribe getTribeByName(String pName) {
        Tribe result = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Getting tribe by name '" + pName + "'");
        }
        if (pName != null && pName.trim().length() > 0) {
            result = mTribesByName.get(pName.trim());
        }
        if (result == null) {
            result = InvalidTribe.getSingleton();
        }
        return result;
    }

    /**
     * Get all units
     */
    public List<UnitHolder> getUnits() {
        return mUnits;
    }
    
    public List<UnitHolder> getSendableUnits() {
        List<UnitHolder> sendUnits = new ArrayList<>();
        sendUnits.addAll(mUnits);
        
        //remove milita
        for(int i = sendUnits.size() - 1; i >= 0; i--) {
            if(sendUnits.get(i).getPlainName().equals("militia")) {
                sendUnits.remove(i);
            }
        }
        return sendUnits;
    }

    public UnitHolder getRandomUnit() {
        int id = (int) (Math.rint(mUnits.size() * Math.random()));
        if (id >= mUnits.size()) {
            if(mUnits.isEmpty()) {
                return new UnitHolder();
            } else {
                id = 0;
            }
        }
        return mUnits.get(id);
    }

    /**
     * Get a unit by its name
     */
    public UnitHolder getUnitByPlainName(String pName) {
        UnitHolder result = null;
        if (pName != null) {
            result = mUnitsByName.get(pName);
        }
        if (result == null) {
            result = UnknownUnit.getSingleton();
        }
        return result;
    }

    /**
     * Get the ID of a unit
     */
    public int getUnitID(String pUnitName) {
        int result = -1;
        int cnt = 0;
        for (UnitHolder unit : mUnits) {
            if (unit.getName().equals(pUnitName)) {
                result = cnt;
                break;
            } else {
                cnt++;
            }
        }
        return result;
    }

    private void fireDataHolderEvents(String pMessage) {
        DataHolderListener[] listeners = mListeners.toArray(new DataHolderListener[]{});
        for (DataHolderListener listener : listeners) {
            listener.fireDataHolderEvent(pMessage);
        }
    }

    private void fireDataLoadedEvents(boolean pSuccess) {
        DataHolderListener[] listeners = mListeners.toArray(new DataHolderListener[]{});
        for (DataHolderListener listener : listeners) {
            listener.fireDataLoadedEvent(pSuccess);
        }
    }
}
