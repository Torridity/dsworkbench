/*
 * AbstractDataReader.java
 *
 * Created on 17.07.2007, 21:49:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.php.DatabaseInterface;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchSettingsDialog;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Charon
 */
public class DataHolder {

    private static Logger logger = Logger.getLogger("DataManager");
    private final int ID_OFF = 0;
    private final int ID_DEF = 1;
    private Village[][] mVillages = null;
    private Hashtable<Integer, Village> mVillagesTable = null;
    private Hashtable<Integer, Ally> mAllies = null;
    private Hashtable<Integer, Tribe> mTribes = null;
    private List<BuildingHolder> mBuildings = null;
    private List<UnitHolder> mUnits = null;
    private List<DataHolderListener> mListeners = null;
    private boolean bAborted = false;
    private static DataHolder SINGLETON = null;
    private boolean loading = false;
    private int currentBonusType = 0;

    public static synchronized DataHolder getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DataHolder();
        }
        return SINGLETON;
    }

    DataHolder() {
        mListeners = new LinkedList<DataHolderListener>();
        initialize();
    }

    /**Clear all data an re-initialize the structures*/
    public void initialize() {
        mVillages = null;
        mVillagesTable = null;
        mAllies = null;
        mTribes = null;
        mBuildings = null;
        mUnits = null;
        System.gc();
        System.gc();

        mVillages = new Village[1000][1000];
        mVillagesTable = new Hashtable<Integer, Village>();
        mAllies = new Hashtable<Integer, Ally>();
        mTribes = new Hashtable<Integer, Tribe>();

        mBuildings = new LinkedList<BuildingHolder>();
        mUnits = new LinkedList<UnitHolder>();
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

    /**Get the server data directory, depending on the selected server*/
    public String getDataDirectory() {
        return Constants.SERVER_DIR + "/" + GlobalOptions.getSelectedServer();
    }

    public int getCurrentBonusType() {
        return currentBonusType;
    }

    /**Check if all needed files are located in the data directory of the selected server*/
    private boolean isDataAvailable() {
        File data = new File(getDataDirectory() + "/" + "serverdata.bin");
        File units = new File(getDataDirectory() + "/" + "units.xml");
        File buildings = new File(getDataDirectory() + "/" + "buildings.xml");
        File settings = new File(getDataDirectory() + "/" + "settings.xml");

        return (data.exists() && units.exists() && buildings.exists() && settings.exists());
    }

    /**Check if server is supported or not. Currently only 1000x1000 servers are allowed
     */
    public boolean serverSupported() {
        fireDataHolderEvents("Prüfe Server Einstellungen");
        try {
            File settings = new File(getDataDirectory() + "/settings.xml");
            if (settings.exists()) {
                /*Document d = JaxenUtils.getDocument(settings);
                Integer mapType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/sector"));*/
                ServerSettings.getSingleton().loadSettings(GlobalOptions.getSelectedServer());
                //Integer mapType = ServerSettings.getSingleton().getCoordType();
               /* if (ServerSettings.getSingleton().getCoordType() != 2) {
                logger.error("Map type '" + ServerSettings.getSingleton().getCoordType() + "' is not supported yet");
                fireDataHolderEvents("Der gewählte Sever wird leider (noch) nicht unterstützt");
                return false;
                }*/
                try {
                    /* Integer bonusType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/bonus_new"));
                    currentBonusType = bonusType;*/
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
                    logger.debug("Server settings do not exist");
                    String sURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
                    new File(DataHolder.getSingleton().getDataDirectory()).mkdirs();
                    fireDataHolderEvents("Lese Server Einstellungen");
                    URL file = new URL(sURL + "/interface.php?func=get_config");
                    logger.debug("Try downloading server settings from " + sURL + "/interface.php?func=get_config");
                    downloadDataFile(file, "settings_tmp.xml");
                    new File("settings_tmp.xml").renameTo(settings);

                    if (!ServerSettings.getSingleton().loadSettings(GlobalOptions.getSelectedServer())) {
                        throw new Exception("Failed to load server settings");
                    }

                    /*Document d = JaxenUtils.getDocument(settings);
                    Integer mapType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/sector"));*/
                    /* Integer mapType = ServerSettings.getSingleton().getCoordType();
                    if (mapType != 2) {
                    logger.error("Map type '" + mapType + "' is not supported yet");
                    fireDataHolderEvents("Der gewählte Sever wird leider (noch) nicht unterstützt");
                    return false;
                    }*/
                    try {
                        //Integer bonusType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/bonus_new"));
                        //  currentBonusType = bonusType;
                        currentBonusType = ServerSettings.getSingleton().getNewBonus();
                    } catch (Exception e) {
                        //bonus_new field not found. Set to old type
                        currentBonusType = 0;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check server settings", e);
            return false;
        }
        return true;
    }

    /**Update the data, optionally by downloading*/
    public boolean loadData(boolean pReload) {
        loading = true;
        try {
            String serverID = GlobalOptions.getSelectedServer();
            logger.info("Calling 'loadData()' for server " + serverID);
            try {
                boolean recreateLocal = false;
                if (serverSupported()) {
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
                                    GlobalOptions.setInternatDataDamaged(true);
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
                                    GlobalOptions.setInternatDataDamaged(true);
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
                                GlobalOptions.setInternatDataDamaged(true);
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
                                fireDataHolderEvents("Erneuter Downloadversuch fehlgeschlagen. Laden wird abgebrochen!");
                                loading = false;
                                fireDataLoadedEvents(false);
                                GlobalOptions.setInternatDataDamaged(true);
                                return false;
                            }
                        }
                    }//end of read local data
                } else {
                    logger.error("Server not supported");
                    fireDataHolderEvents("Server nicht unterstützt");
                    fireDataLoadedEvents(false);
                    GlobalOptions.setInternatDataDamaged(true);
                    return false;
                }

                //setting internal data to be valid
                GlobalOptions.setInternatDataDamaged(false);

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
                parseBuildings();
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

    public boolean loadLiveData() {
        loading = true;
        try {
            String serverID = GlobalOptions.getSelectedServer();
            logger.info("Calling 'loadLiveData()' for server " + serverID);
            try {
                boolean recreateLocal = false;
                if (serverSupported()) {
                    if (!GlobalOptions.isOfflineMode()) {
                        fireDataHolderEvents("Download der aktuellen Weltdaten von die-staemme.de gestartet");
                        logger.debug(" - Initiating full reload of live data");
                        if (downloadLiveData()) {
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
                                    GlobalOptions.setInternatDataDamaged(true);
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
                                    fireDataHolderEvents("Downloadversuch vom DS Workbench Server fehlgeschlagen. Laden wird abgebrochen!");
                                    loading = false;
                                    fireDataLoadedEvents(false);
                                    GlobalOptions.setInternatDataDamaged(true);
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
                                GlobalOptions.setInternatDataDamaged(true);
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
                                fireDataHolderEvents("Erneuter Downloadversuch fehlgeschlagen. Laden wird abgebrochen!");
                                loading = false;
                                fireDataLoadedEvents(false);
                                GlobalOptions.setInternatDataDamaged(true);
                                return false;
                            }
                        }
                    }//end of read local data
                } else {
                    logger.error("Server not supported");
                    fireDataHolderEvents("Server nicht unterstützt");
                    fireDataLoadedEvents(false);
                    GlobalOptions.setInternatDataDamaged(true);
                    return false;
                }

                //setting internal data to be valid
                GlobalOptions.setInternatDataDamaged(false);

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
                parseBuildings();
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

    public synchronized boolean isLoading() {
        return loading;
    }

    private boolean createLocalDataCopy(String pServerDir) {
        try {
            BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(pServerDir + "/serverdata.bin"))));
            logger.info("Writing villages to " + pServerDir + "/serverdata.bin");
            bout.write("<villages>\n");
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 1000; j++) {
                    Village v = mVillages[i][j];
                    if (v != null) {
                        bout.write(v.toPlainData() + "\n");
                    }
                }
            }

            logger.info("Writing tribes");
            bout.write("<tribes>\n");
            Enumeration<Integer> e = mTribes.keys();
            while (e.hasMoreElements()) {
                Tribe t = mTribes.get(e.nextElement());
                bout.write(t.toPlainData() + "\n");
            }

            logger.info("Writing allies");
            bout.write("<allies>\n");
            e = mAllies.keys();
            while (e.hasMoreElements()) {
                Ally a = mAllies.get(e.nextElement());
                bout.write(a.toPlainData() + "\n");
            }
            bout.flush();
            bout.close();
        } catch (Exception e) {
            logger.error("Failed to store local data", e);
            return false;
        }
        logger.debug("Local data successfully written");
        return true;
    }

    public boolean readLocalDataCopy(String pServerDir) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pServerDir + "/serverdata.bin"))));
            String line = "";
            int step = 0;
            int ac = 0;
            int vc = 0;
            int tc = 0;
            initialize();
            while ((line = r.readLine()) != null) {
                if (line.equals("<villages>")) {
                    logger.info("Reading villages");
                    step = 1;
                } else if (line.equals("<tribes>")) {
                    logger.info("Reading tribes");
                    step = 2;
                } else if (line.equals("<allies>")) {
                    logger.info("Reading allies");
                    step = 3;
                }

                switch (step) {
                    case 1: {
                        Village v = Village.parseFromPlainData(line);
                        if (v != null) {
                            mVillages[v.getX()][v.getY()] = v;
                            vc++;
                        }
                        break;
                    }
                    case 2: {
                        Tribe t = Tribe.parseFromPlainData(line);
                        if (t != null) {
                            mTribes.put(t.getId(), t);
                            tc++;
                        }
                        break;
                    }
                    case 3: {
                        Ally a = Ally.parseFromPlainData(line);
                        if (a != null) {
                            mAllies.put(a.getId(), a);
                            ac++;
                        }
                        break;
                    }
                    default: {
                        //sth. else!?
                    }
                }

            }

            if (vc == 0 || ac == 0 || tc == 0) {
                //data obviously invalid
                logger.error("#villages | #allies | #tribes is 0");
                return false;
            }
            logger.info("Read " + vc + " villages");
            logger.info("Read " + ac + " allies");
            logger.info("Read " + tc + " tribes");
            r.close();
        } catch (Exception e) {
            logger.error("Failed loading local data", e);
            return false;
        }
        return true;
    }

    /**Download all needed data files (villages, tribes, allies, kills, settings)*/
    private boolean downloadData() {
        URL file = null;
        String serverID = GlobalOptions.getSelectedServer();
        String serverDir = getDataDirectory();
        logger.info("Using server dir '" + serverDir + "'");
        new File(serverDir).mkdirs();

        try {
            // <editor-fold defaultstate="collapsed" desc="Account check">
            //check account
            String accountName = GlobalOptions.getProperty("account.name");
            String accountPassword = GlobalOptions.getProperty("account.password");
            if ((accountName == null) || (accountPassword == null)) {
                logger.error("No account name or password set");
                fireDataHolderEvents("Account Name oder Passwort sind nicht gesetzt oder ungültig. Bitte überprüfe deine Accounteinstellungen.");
                return false;
            }
            if (DatabaseInterface.checkUser(accountName, accountPassword) != DatabaseInterface.ID_SUCCESS) {
                logger.error("Failed to validate account (Wrong username or password?)");
                return false;
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="DS Workbench Version check">
            int ret = DatabaseInterface.isVersionAllowed();
            if (ret != DatabaseInterface.ID_SUCCESS) {
                if (ret != DatabaseInterface.ID_VERSION_NOT_ALLOWED) {
                    logger.error("Current version is not allowed any longer");
                    fireDataHolderEvents("Deine DS Workbench Version ist zu alt. Bitte lade dir die aktuelle Version herunter.");
                } else {
                    String error = DatabaseInterface.getProperty("data_error_message");
                    logger.error("Update currently not allowed by server (Message: '" + error + "'");
                    fireDataHolderEvents("Momentan sind leider keine Updates möglich.");
                    fireDataHolderEvents("Folgender Grund wurde vom Systemadministrator angegeben: '" + error + "'");
                }

                return false;

            }
            //</editor-fold>

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
            new File("settings_tmp.xml").renameTo(target);

            if (!serverSupported()) {
                return false;
            }
            //</editor-fold>

            long serverDataVersion = DatabaseInterface.getServerDataVersion(serverID);
            long userDataVersion = DatabaseInterface.getUserDataVersion(accountName, serverID);
            String downloadURL = DatabaseInterface.getDownloadURL(serverID);
            if ((userDataVersion == serverDataVersion) && isDataAvailable()) {
                //no update needed
                logger.info("No update needed");
                return true;
            } else if ((userDataVersion == -666) || (serverDataVersion != userDataVersion) || !isDataAvailable()) {
                //full download if no download made yet or diff too large
                //load villages
                logger.info("Downloading new data version from " + downloadURL);
                //clear all data structures
                initialize();

                // <editor-fold defaultstate="collapsed" desc=" Load villages ">

                fireDataHolderEvents("Lade Dörferliste");
                file = new URL(downloadURL + "/village.txt.gz");

                logger.debug(" + Start reading villages");
                downloadDataFile(file, "village.tmp");
                logger.debug(" - Finished reading villages");

                BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("village.tmp"))));
                String line = "";
                logger.debug(" + Start parsing villages");
                while ((line = r.readLine()) != null) {
                    line = line.replaceAll(",,", ", ,");
                    Village v = Village.parseFromPlainData(line);
                    try {
                        mVillages[v.getX()][v.getY()] = v;
                    } catch (Exception e) {
                        //ignore invalid village
                    }
                }
                r.close();
                logger.debug(" - Finished parsing villages");
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load tribes ">

                fireDataHolderEvents("Lade Spielerliste");

                file = new URL(downloadURL + "/tribe.txt.gz");
                logger.debug(" + Start reading tribes");
                downloadDataFile(file, "tribe.tmp");
                logger.debug(" - Finished reading tribes");

                r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("tribe.tmp"))));

                line = "";
                logger.debug(" + Start parsing tribes");
                try {
                    while ((line = r.readLine()) != null) {
                        line = line.replaceAll(",,", ", ,");
                        Tribe t = Tribe.parseFromPlainData(line);
                        try {
                            mTribes.put(t.getId(), t);
                        } catch (Exception e) {
                            //ignore invalid tribe
                        }
                    }
                } catch (Throwable t) {
                }
                r.close();
                logger.debug(" - Finished parsing tribes");
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load allies ">
                fireDataHolderEvents("Lade Stämmeliste");
                file = new URL(downloadURL + "/ally.txt.gz");
                logger.debug(" + Start reading allies");
                downloadDataFile(file, "ally.tmp");
                logger.debug(" - Finished reading allies");

                r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("ally.tmp"))));
                line = "";
                logger.debug(" + Start parsing allies");
                while ((line = r.readLine()) != null) {
                    line = line.replaceAll(",,", ", ,");
                    Ally a = Ally.parseFromPlainData(line);
                    try {
                        mAllies.put(a.getId(), a);
                    } catch (Exception e) {
                        //ignore invalid ally
                    }
                }
                logger.debug(" - Finished parsing allies");
                r.close();

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load conquers off ">
                fireDataHolderEvents("Lese besiegte Gegner (Angriff)...");
                target = new File(serverDir + "/kill_att.txt.gz");
                file = new URL(downloadURL + "/kill_att.txt.gz");
                logger.debug(" + Downloading conquers (off)");
                downloadDataFile(file, "kill_att.tmp");
                if (target.exists()) {
                    target.delete();
                }
                new File("kill_att.tmp").renameTo(target);
                logger.debug(" - Finished downloading conquers (off)");
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load conquers def ">
                fireDataHolderEvents("Lese besiegte Gegner (Verteidigung)...");
                target = new File(serverDir + "/kill_def.txt.gz");
                file = new URL(downloadURL + "/kill_def.txt.gz");
                logger.debug(" + Downloading conquers (def)");
                downloadDataFile(file, "kill_def.tmp");
                if (target.exists()) {
                    target.delete();
                }
                new File("kill_def.tmp").renameTo(target);
                logger.debug(" - Finished downloading conquers (def)");
                // </editor-fold>

                //finally register user for server if not available yet
                if (userDataVersion == -666) {
                    DatabaseInterface.registerUserForServer(accountName, serverID);
                }
            }

            // <editor-fold defaultstate="collapsed" desc="Direct download from DS-Servers">
            //download unit information, but only once
            target = new File(serverDir + "/units.xml");
            if (!target.exists()) {
                logger.debug("Loading unit config file from server");
                fireDataHolderEvents("Lade Information über Einheiten");
                file = new URL(sURL + "/interface.php?func=get_unit_info");
                downloadDataFile(file, "units_tmp.xml");

                new File("units_tmp.xml").renameTo(target);
            }

            //download building information, but only once
            target = new File(serverDir + "/buildings.xml");
            if (!target.exists()) {
                logger.debug("Loading building config file from server");
                fireDataHolderEvents("Lade Information über Gebäude");
                file = new URL(sURL + "/interface.php?func=get_building_info");
                downloadDataFile(file, "buildings_tmp.xml");
                new File("buildings_tmp.xml").renameTo(target);
            }
            //</editor-fold>

            DatabaseInterface.updateDataVersion(accountName, serverID);
            fireDataHolderEvents("Download erfolgreich beendet.");
        } catch (Throwable t) {
            fireDataHolderEvents("Download fehlgeschlagen.");
            logger.error("Failed to download data", t);
            return false;
        }
        return true;
    }

    private boolean downloadLiveData() {
        URL file = null;
        String serverID = GlobalOptions.getSelectedServer();
        String serverDir = getDataDirectory();
        logger.info("Using server dir '" + serverDir + "'");
        new File(serverDir).mkdirs();

        try {
            // <editor-fold defaultstate="collapsed" desc="Account check">
            //check account
            String accountName = GlobalOptions.getProperty("account.name");
            String accountPassword = GlobalOptions.getProperty("account.password");
            if ((accountName == null) || (accountPassword == null)) {
                logger.error("No account name or password set");
                fireDataHolderEvents("Account Name oder Passwort sind nicht gesetzt oder ungültig. Bitte überprüfe deine Accounteinstellungen.");
                return false;
            }
            if (DatabaseInterface.checkUser(accountName, accountPassword) != DatabaseInterface.ID_SUCCESS) {
                logger.error("Failed to validate account (Wrong username or password?)");
                return false;
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="DS Workbench Version check">
            int ret = DatabaseInterface.isVersionAllowed();
            if (ret != DatabaseInterface.ID_SUCCESS) {
                if (ret != DatabaseInterface.ID_VERSION_NOT_ALLOWED) {
                    logger.error("Current version is not allowed any longer");
                    fireDataHolderEvents("Deine DS Workbench Version ist zu alt. Bitte lade dir die aktuelle Version herunter.");
                } else {
                    String error = DatabaseInterface.getProperty("data_error_message");
                    logger.error("Update currently not allowed by server (Message: '" + error + "'");
                    fireDataHolderEvents("Momentan sind leider keine Updates möglich.");
                    fireDataHolderEvents("Folgender Grund wurde vom Systemadministrator angegeben: '" + error + "'");
                }

                return false;

            }
            //</editor-fold>

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
            new File("settings_tmp.xml").renameTo(target);

            if (!serverSupported()) {
                return false;
            }
            //</editor-fold>

            //load villages
            logger.info("Downloading new data version from " + sURL);
            //clear all data structures
            initialize();

            // <editor-fold defaultstate="collapsed" desc=" Load villages ">

            fireDataHolderEvents("Lade Dörferliste");
            file = new URL(sURL + "/map//village.txt.gz");

            logger.debug(" + Start reading villages");
            downloadDataFile(file, "village.tmp");
            logger.debug(" - Finished reading villages");

            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("village.tmp"))));
            String line = "";
            logger.debug(" + Start parsing villages");
            while ((line = r.readLine()) != null) {
                line = line.replaceAll(",,", ", ,");
                Village v = Village.parseFromPlainData(line);
                try {
                    mVillages[v.getX()][v.getY()] = v;
                } catch (Exception e) {
                    //ignore invalid village
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
            try {
                while ((line = r.readLine()) != null) {
                    line = line.replaceAll(",,", ", ,");
                    Tribe t = Tribe.parseFromPlainData(line);
                    try {
                        mTribes.put(t.getId(), t);
                    } catch (Exception e) {
                        //ignore invalid tribe
                        }
                }
            } catch (Throwable t) {
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
                try {
                    mAllies.put(a.getId(), a);
                } catch (Exception e) {
                    //ignore invalid ally
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
            new File("kill_att.tmp").renameTo(target);
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
            new File("kill_def.tmp").renameTo(target);
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

                new File("units_tmp.xml").renameTo(target);
            }

            //download building information, but only once
            target = new File(serverDir + "/buildings.xml");
            if (!target.exists()) {
                logger.debug("Loading building config file from server");
                fireDataHolderEvents("Lade Information über Gebäude");
                file = new URL(sURL + "/interface.php?func=get_building_info");
                downloadDataFile(file, "buildings_tmp.xml");
                new File("buildings_tmp.xml").renameTo(target);
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

    /**Merge all data into the village data structure to ease searching*/
    private void mergeData() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                Village current = mVillages[i][j];

                if (current != null) {
                    //set tribe of village
                    Tribe t = mTribes.get(current.getTribeID());
                    //set tribe of village
                    current.setTribe(t);
                    if (t != null) {
                        //add village to tribe
                        t.addVillage(current);
                        Ally currentAlly = mAllies.get(t.getAllyID());
                        //set ally of tribe
                        t.setAlly(currentAlly);
                        if (currentAlly != null) {
                            //add tribe to ally
                            currentAlly.addTribe(t);
                        }
                    }
                    mVillagesTable.put(current.getId(), current);
                }
            }
        }
        logger.debug("Removing empty allies");
        Enumeration<Integer> allyKeys = mAllies.keys();
        List<Ally> toRemove = new LinkedList<Ally>();
        while (allyKeys.hasMoreElements()) {
            Ally a = mAllies.get(allyKeys.nextElement());
            if (a.getTribes() == null || a.getTribes().isEmpty()) {
                toRemove.add(a);
            }
        }
        for (Ally a : toRemove) {
            mAllies.remove(a.getId());
        }

        logger.debug("Removed " + toRemove.size() + " empty allies");
    }

    /**Download one single file from a URL*/
    private void downloadDataFile(URL pSource, String pLocalName) throws Exception {
        URLConnection ucon = pSource.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
        ucon.setConnectTimeout(3000);
        ucon.setReadTimeout(20000);
        FileOutputStream tempWriter = new FileOutputStream(pLocalName);
        InputStream isr = ucon.getInputStream();

        int bytes = 0;
        byte[] data = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int sum = 0;
        while (bytes != -1) {

            if (bytes != -1) {
                result.write(data, 0, bytes);

            }

            bytes = isr.read(data);
            sum += bytes;
            if (sum % 500 == 0) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }
        }

        tempWriter.write(result.toByteArray());
        tempWriter.flush();
        try {
            isr.close();
            tempWriter.close();
        } catch (Exception e) {
        }
    }

    /**Parse a line of a conquered units file and set the data for the associated tribe*/
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

    /**Parse the list of units*/
    private void parseUnits() {
        String unitFile = getDataDirectory() + "/units.xml";
        //buildingsFile += "/units.xml";
        logger.debug("Loading units");
        try {
            Document d = JaxenUtils.getDocument(new File(unitFile));
            d = JaxenUtils.getDocument(new File(unitFile));
            List<Element> l = JaxenUtils.getNodes(d, "/config/*");
            for (Element e : l) {
                try {
                    mUnits.add(new UnitHolder(e));
                } catch (Exception inner) {
                    logger.error("Failed loading unit", inner);
                }
            }
        } catch (Exception outer) {
            logger.error("Failed to load units", outer);
            fireDataHolderEvents("Laden der Einheiten fehlgeschlagen");
        }
    }

    /**Parse the list of buildings*/
    public void parseBuildings() {
        /* String buildingsFile = getDataDirectory();
        buildingsFile += "/buildings.xml";
        try {
        Document d = JaxenUtils.getDocument(new File(buildingsFile));
        d = JaxenUtils.getDocument(new File(buildingsFile));
        List<Element> l = JaxenUtils.getNodes(d, "/config/*");
        for (Element e : l) {
        try {
        mBuildings.add(new BuildingHolder(e));
        } catch (Exception inner) {
        }
        }
        } catch (Exception outer) {
        logger.error("Failed to load buildings", outer);
        fireDataHolderEvents("Laden der Gebäude fehlgeschlagen");
        }*/
    }

    /**Get all villages<BR>
     * !!Attention!!<B>This call blocks while loading data. It is only intended to be used externally</B> !!Attention!! 
     */
    public synchronized Village[][] getVillages() {
        if (isLoading()) {
            //block getting villages while loading to avoid nullpointer exceptions
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }
        }
        return mVillages;
    }

    public List<Village> getVillagesInRegion(Point pStart, Point pEnd) {
        List<Village> marked = new LinkedList<Village>();
        try {
            int xStart = (pStart.x < pEnd.x) ? pStart.x : pEnd.x;
            int xEnd = (pEnd.x > pStart.x) ? pEnd.x : pStart.x;
            int yStart = (pStart.y < pEnd.y) ? pStart.y : pEnd.y;
            int yEnd = (pEnd.y > pStart.y) ? pEnd.y : pStart.y;
            boolean showBarbarian = true;
            try {
                showBarbarian = Boolean.parseBoolean(GlobalOptions.getProperty("show.barbarian"));
            } catch (Exception e) {
                showBarbarian = true;
            }

            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    Village v = getVillages()[x][y];
                    if ((v != null && v.getTribe() == null) && !showBarbarian) {
                        //dont select barbarians if they are not visible
                    } else {
                        if (v != null && !marked.contains(v)) {
                            marked.add(v);
                        }
                    }
                }
            }
            Collections.sort(marked, Village.ALLY_TRIBE_VILLAGE_COMPARATOR);
        } catch (Exception e) {
            //occurs if no rect was opened by selection tool -> ignore
        }
        return marked;
    }

    /**Get villages as a hashtable ordered by IDs*/
    public synchronized Hashtable<Integer, Village> getVillagesById() {
        if (isLoading()) {
            //block getting villages while loading to avoid nullpointer exceptions
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }
        }
        return mVillagesTable;
    }

    public void removeTempData() {
        /*  mVillagesTable.clear();
        mVillagesTable = null;*/
        System.gc();
    }

    /**Get all allies*/
    public Hashtable<Integer, Ally> getAllies() {
        return mAllies;
    }

    /**Search the ally list for the ally with the provided name*/
    public Ally getAllyByName(String pName) {
        Enumeration<Integer> ids = mAllies.keys();
        while (ids.hasMoreElements()) {
            Ally a = mAllies.get(ids.nextElement());
            if (a != null) {
                if (a.getName() != null) {
                    if (a.getName().equals(pName)) {
                        return a;
                    }

                }
            }
        }
        return null;
    }

    /**Get all tribes*/
    public Hashtable<Integer, Tribe> getTribes() {
        return mTribes;
    }

    /**Search the tribes list for the tribe with the provided name*/
    public Tribe getTribeByName(String pName) {
        Enumeration<Integer> ids = getTribes().keys();
        while (ids.hasMoreElements()) {
            Tribe t = getTribes().get(ids.nextElement());
            if (t != null) {
                if (t.getName() != null) {
                    if (t.getName().equals(pName)) {
                        return t;
                    }

                }
            }
        }
        return null;
    }

    /**Get all units*/
    public List<UnitHolder> getUnits() {
        return mUnits;
    }

    /**Get a unit by its name*/
    public UnitHolder getUnitByPlainName(String pName) {
        for (UnitHolder u : getUnits()) {
            if (u.getPlainName().equals(pName)) {
                return u;
            }
        }
        return null;
    }

    /**Get the ID of a unit*/
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

    /**Get the list of buildings*/
    public List<BuildingHolder> getBuildings() {
        return mBuildings;
    }

    public synchronized void fireDataHolderEvents(String pMessage) {
        DataHolderListener[] listeners = mListeners.toArray(new DataHolderListener[]{});
        for (DataHolderListener listener : listeners) {
            listener.fireDataHolderEvent(pMessage);
        }
    }

    public synchronized void fireDataLoadedEvents(boolean pSuccess) {
        DataHolderListener[] listeners = mListeners.toArray(new DataHolderListener[]{});
        for (DataHolderListener listener : listeners) {
            listener.fireDataLoadedEvent(pSuccess);
        }
    }
}
