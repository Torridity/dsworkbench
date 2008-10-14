/*
 * AbstractDataReader.java
 *
 * Created on 17.07.2007, 21:49:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.db.DatabaseAdapter;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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
 *
 * @author Charon
 */
public class DataHolder {

    private static Logger logger = Logger.getLogger(DataHolder.class);
    private final int ID_ATT = 0;
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
        mListeners.add(pListener);
    }

    public synchronized void removeDataHolderListener(DataHolderListener pListener) {
        mListeners.remove(pListener);
    }

    /**Get the server data directory, depending on the selected server*/
    public String getDataDirectory() {
        return Constants.SERVER_DIR + "/" + GlobalOptions.getSelectedServer();
    }

    /**Check if all needed files are located in the data directory of the selected server*/
    private boolean isDataAvailable() {
        File data = new File(getDataDirectory() + "/" + "serverdata.bin");
        File units = new File(getDataDirectory() + "/" + "units.xml");
        File buildings = new File(getDataDirectory() + "/" + "buildings.xml");
        File settings = new File(getDataDirectory() + "/" + "settings.xml");

        return (data.exists() && units.exists() && buildings.exists() && settings.exists());
    }

    /**Check if server is supported or not. Currently only 1000x1000 servers are allowed*/
    public boolean serverSupported() {
        fireDataHolderEvents("Prüfe Server Einstellungen");
        try {
            File settings = new File(getDataDirectory() + "/settings.xml");
            if (settings.exists()) {
                Document d = JaxenUtils.getDocument(settings);
                Integer mapType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/sector"));
                if (mapType != 2) {
                    logger.error("Map type '" + mapType + "' is not supported yet");
                    fireDataHolderEvents("Der gewählte Sever wird leider (noch) nicht unterstützt");
                    return false;
                }
            } else {
                if (GlobalOptions.isOfflineMode()) {
                    fireDataHolderEvents("Servereinstellungen nicht gefunden. Download im Offline-Modus nicht möglich.");
                    return false;
                } else {
                    //download settings.xml
                    String sURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
                    new File(DataHolder.getSingleton().getDataDirectory()).mkdirs();
                    fireDataHolderEvents("Lese Server Einstellungen");
                    URL file = new URL(sURL + "/interface.php?func=get_config");
                    downloadDataFile(file, "settings_tmp.xml");
                    new File("settings_tmp.xml").renameTo(settings);
                    Document d = JaxenUtils.getDocument(settings);
                    Integer mapType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/sector"));
                    if (mapType != 2) {
                        logger.error("Map type '" + mapType + "' is not supported yet");
                        fireDataHolderEvents("Der gewählte Sever wird leider (noch) nicht unterstützt");
                        return false;
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
        String serverID = GlobalOptions.getSelectedServer();
        logger.info("Calling 'loadData()' for server " + serverID);
        try {
            boolean recreateLocal = false;
            //check if download was requested
            if (pReload) {
                //completely reload data
                fireDataHolderEvents("Daten werden heruntergeladen...");
                //try to download
                if (!downloadData()) {
                    fireDataHolderEvents("Download abgebrochen/fehlgeschlagen!");
                    fireDataLoadedEvents(false);
                    return false;
                } else {
                    //rebuild local data
                    recreateLocal = true;
                }
            } else {
                //check if local loading possible
                if (!isDataAvailable()) {
                    logger.error("Local data not available. Try to download data");
                    fireDataHolderEvents("Lokale Kopie nicht gefunden. Lade Daten vom Server");
                    if (!downloadData()) {
                        logger.fatal("Download failed. No data available at the moment");
                        fireDataHolderEvents("Download abgebrochen/fehlgeschlagen");
                        fireDataLoadedEvents(false);
                        return false;
                    } else {
                        recreateLocal = true;
                    }
                } else if (!serverSupported()) {
                    logger.error("Local data available but server not supported");
                    fireDataLoadedEvents(false);
                    return false;
                } else {
                    //load data from local copy
                    fireDataHolderEvents("Lade lokale Kopie");
                    if (!readLocalDataCopy(getDataDirectory())) {
                        //local copy invalid, download data
                        new File(getDataDirectory() + "/" + "serverdata.bin").delete();
                        logger.error("Failed to read local copy from " + getDataDirectory() + ". Try to download data");
                        fireDataHolderEvents("Lokale Kopie nicht gefunden. Lade Daten vom Server");
                        if (!downloadData()) {
                            logger.fatal("Download failed. No data available at the moment");
                            fireDataHolderEvents("Download abgebrochen/fehlgeschlagen");
                            return false;
                        } else {
                            recreateLocal = true;
                        }
                    }
                }
            }

            logger.info("Reading conquered units");

            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_att.txt.gz"))));
            String line = "";
            while ((line = r.readLine()) != null) {
                try {
                    parseConqueredLine(line, ID_ATT);
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
        } catch (Exception e) {
            fireDataHolderEvents("Fehler beim Lesen der Daten.");
            logger.error("Failed to read server data", e);
            if (bAborted) {
                fireDataLoadedEvents(false);
                return false;
            }
        }

        fireDataLoadedEvents(true);
        return true;
    }

    private boolean createLocalDataCopy(String pServerDir) {

        try {
            BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(pServerDir + "/serverdata.bin"))));

            logger.info("Writing villages to " + pServerDir + "/serverdata.bin");
            bout.write("<villages>\n");
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 1000; j++) {
                    Village v = getVillages()[i][j];
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
                            mVillagesTable.put(v.getId(), v);
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
            if (DatabaseAdapter.checkUser(accountName, accountPassword) != DatabaseAdapter.ID_SUCCESS) {
                logger.error("Failed to validate account (Wrong username or password?)");
                return false;
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="DS Workbench Version check">
            int ret = DatabaseAdapter.isVersionAllowed();
            if (ret != DatabaseAdapter.ID_SUCCESS) {
                if (ret != DatabaseAdapter.ID_UPDATE_NOT_ALLOWED) {
                    logger.error("Current version is not allowed any longer");
                    fireDataHolderEvents("Deine DS Workbench Version ist zu alt. Bitte lade dir die aktuelle Version herunter.");
                } else {
                    String error = DatabaseAdapter.getPropertyValue("data_error_message");
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

            fireDataHolderEvents("Lese Server Einstellungen");
            File target = new File(serverDir + "/settings.xml");
            if (!target.exists()) {
                file = new URL(sURL + "/interface.php?func=get_config");
                downloadDataFile(file, "settings_tmp.xml");
                new File("settings_tmp.xml").renameTo(target);
            }

            if (!serverSupported()) {
                return false;
            }
            //</editor-fold>

            long serverDataVersion = DatabaseAdapter.getDataVersion(serverID);
            long userDataVersion = DatabaseAdapter.getUserDataVersion(accountName, serverID);
            String downloadURL = DatabaseAdapter.getServerDownloadURL(serverID);
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
                URL u = new URL(downloadURL + "/village.txt.gz");

                BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.openConnection().getInputStream())));
                String line = "";
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

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load tribes ">

                fireDataHolderEvents("Lade Spielerliste");
                u = new URL(downloadURL + "/tribe.txt.gz");
                r = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.openConnection().getInputStream())));
                line = "";
                while ((line = r.readLine()) != null) {
                    line = line.replaceAll(",,", ", ,");
                    Tribe t = Tribe.parseFromPlainData(line);
                    try {
                        mTribes.put(t.getId(), t);
                    } catch (Exception e) {
                        //ignore invalid tribe
                    }
                }
                r.close();

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load allies ">

                fireDataHolderEvents("Lade Stämmeliste");
                u = new URL(downloadURL + "/ally.txt.gz");
                r = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.openConnection().getInputStream())));
                line = "";
                while ((line = r.readLine()) != null) {
                    line = line.replaceAll(",,", ", ,");
                    Ally a = Ally.parseFromPlainData(line);
                    try {
                        mAllies.put(a.getId(), a);
                    } catch (Exception e) {
                        //ignore invalid ally
                    }
                }
                r.close();

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load conquers off ">
                fireDataHolderEvents("Lese besiegte Gegner (Angriff)...");
                target = new File(serverDir + "/kill_att.txt.gz");
                if (!target.exists()) {
                    file = new URL(downloadURL + "/kill_att.txt.gz");
                    downloadDataFile(file, "kill_att.tmp");
                    new File("kill_att.tmp").renameTo(target);
                }
                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc=" Load conquers def ">
                fireDataHolderEvents("Lese besiegte Gegner (Verteidigung)...");
                target = new File(serverDir + "/kill_def.txt.gz");
                if (!target.exists()) {
                    file = new URL(downloadURL + "/kill_def.txt.gz");
                    downloadDataFile(file, "kill_def.tmp");
                    new File("kill_def.tmp").renameTo(target);
                }
                // </editor-fold>

                //finally register user for server if not available yet
                if (userDataVersion == -666) {
                    DatabaseAdapter.registerUserForServer(accountName, serverID);
                }
            }

            // <editor-fold defaultstate="collapsed" desc=" DEPRECATED DIFF ">
            /*else {
            logger.info("Loading differential updates");
            //normal update of all diffs
            for (int i = userDataVersion + 1; i <= serverDataVersion; i++) {
            logger.debug("Loading part #" + i);
            //download every diff between user version and server version
            fireDataHolderEvents("Lade Update #" + i);
            URL u = new URL(downloadURL + "/village" + i + ".txt.gz");
            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.openConnection().getInputStream())));
            String line = "";
            while ((line = r.readLine()) != null) {
            line = line.replaceAll(",,", ", ,");
            try {
            int id = Integer.parseInt(line.substring(0, line.indexOf(",")));
            Village v = mVillagesTable.get(id);
            if (v == null) {
            //new village
            Village vnew = Village.parseFromPlainData(line);
            mVillages[vnew.getX()][vnew.getY()] = vnew;
            mVillagesTable.put(id, vnew);
            } else {
            v.updateFromDiff(line);
            }
            
            } catch (Exception e) {
            //ignore invalid ally
            }
            }
            r.close();
            
            u = new URL(downloadURL + "/tribe" + i + ".txt.gz");
            r = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.openConnection().getInputStream())));
            line = "";
            
            while ((line = r.readLine()) != null) {
            line = line.replaceAll(",,", ", ,");
            try {
            if (line.indexOf(",") < 0) {
            //tribe to remove
            int id = Integer.parseInt(line.trim());
            mTribes.remove(id);
            } else {
            int id = Integer.parseInt(line.substring(0, line.indexOf(",")));
            Tribe t = mTribes.get(id);
            if (t == null) {
            //new tribe
            mTribes.put(id, Tribe.parseFromPlainData(line));
            } else {
            t.updateFromDiff(line);
            }
            }
            } catch (Exception e) {
            //ignore invalid ally
            }
            }
            r.close();
            
            u = new URL(downloadURL + "/ally" + i + ".txt.gz");
            r = new BufferedReader(new InputStreamReader(new GZIPInputStream(u.openConnection().getInputStream())));
            line = "";
            while ((line = r.readLine()) != null) {
            line = line.replaceAll(",,", ", ,");
            try {
            if (line.indexOf(",") < 0) {
            //ally to remove
            int id = Integer.parseInt(line.trim());
            mAllies.remove(id);
            } else {
            int id = Integer.parseInt(line.substring(0, line.indexOf(",")));
            Ally a = mAllies.get(id);
            if (a == null) {
            //new ally 
            a = Ally.parseFromPlainData(line);
            if (a != null) {
            mAllies.put(a.getId(), a);
            }
            } else {
            a.updateFromDiff(line);
            }
            }
            } catch (Exception e) {
            //ignore invalid ally
            }
            }
            r.close();
            }
            
            logger.debug("Loading conquered units");
            //finally update the bash points
            //load conquers off
            fireDataHolderEvents("Lese besiegte Gegner (Angriff)...");
            target = new File(serverDir + "/kill_att.txt.gz");
            if (!target.exists()) {
            file = new URL(downloadURL + "/kill_att.txt.gz");
            downloadDataFile(file, "kill_att.tmp");
            new File("kill_att.tmp").renameTo(target);
            }
            
            fireDataHolderEvents("Lese besiegte Gegner (Verteidigung)...");
            target = new File(serverDir + "/kill_def.txt.gz");
            if (!target.exists()) {
            file = new URL(downloadURL + "/kill_def.txt.gz");
            downloadDataFile(file, "kill_def.tmp");
            new File("kill_def.tmp").renameTo(target);
            }
            }*/
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

            DatabaseAdapter.updateUserDataVersion(accountName, serverID, serverDataVersion);
            fireDataHolderEvents("Download erfolgreich beendet.");
        } catch (Exception e) {
            fireDataHolderEvents("Download fehlgeschlagen.");
            logger.error("Failed to download data", e);
            return false;
        }
        return true;
    }

    /**Merge all data into the village data structure to ease searching*/
    private void mergeData() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                Village current = getVillages()[i][j];
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
                }
            }
        }
    }

    /**Download one single file from a URL*/
    private void downloadDataFile(URL pSource, String pLocalName) throws Exception {
        URLConnection ucon = pSource.openConnection();
        FileOutputStream tempWriter = new FileOutputStream(pLocalName);
        InputStream isr = ucon.getInputStream();
        int bytes = 0;
        while (bytes != -1) {
            byte[] data = new byte[1024];
            bytes = isr.read(data);
            if (bytes != -1) {
                tempWriter.write(data, 0, bytes);
            }
        }
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
            Tribe t = getTribes().get(Integer.parseInt(tribeID));
            if (pType == ID_ATT) {
                t.setKillsAtt(Integer.parseInt(kills));
                t.setRankAtt(Integer.parseInt(rank));
            } else {
                t.setKillsDef(Integer.parseInt(kills));
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
        String buildingsFile = getDataDirectory();
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
        }
    }

    /**Get all villages*/
    public synchronized Village[][] getVillages() {
        return mVillages;
    }

    /**Get villages as a hashtable ordered by IDs*/
    public Hashtable<Integer, Village> getVillagesById() {
        return mVillagesTable;
    }

    /**Get all allies*/
    public Hashtable<Integer, Ally> getAllies() {
        return mAllies;
    }

    /**Search the ally list for the ally with the provided name*/
    public Ally getAllyByName(String pName) {
        Enumeration<Integer> ids = getAllies().keys();
        while (ids.hasMoreElements()) {
            Ally a = getAllies().get(ids.nextElement());
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
