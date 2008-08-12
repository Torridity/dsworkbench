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
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
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
    private String sServerBaseDir = "./servers";

    public DataHolder() {
        mListeners = new LinkedList<DataHolderListener>();
        initialize();
    }

    /**Clear all data an re-initialize the structures*/
    public void initialize() {
        mVillages = new Village[1000][1000];

        mVillagesTable = new Hashtable<Integer, Village>();
        mAllies = new Hashtable<Integer, Ally>();
        mTribes = new Hashtable<Integer, Tribe>();

        mBuildings = new LinkedList<BuildingHolder>();
        mUnits = new LinkedList<UnitHolder>();
        File serverDir = new File(sServerBaseDir);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
        System.gc();
    }

    public synchronized void addListener(DataHolderListener pListener) {
        mListeners.add(pListener);
    }

    public synchronized void removeListener(DataHolderListener pListener) {
        mListeners.remove(pListener);
    }

    /**Get the listof locally stored servers*/
    public String[] getLocalServers() {
        List<String> servers = new LinkedList<String>();
        for (File serverDir : new File(sServerBaseDir).listFiles()) {
            if (serverDir.isDirectory()) {
                servers.add(serverDir.getName());
            }
        }
        return servers.toArray(new String[0]);
    }

    /**Get the server data directory, depending on the selected server*/
    public String getDataDirectory() {
        return sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
    }

    /**Check if all needed files are located in the data directory of the selected server*/
    private boolean isDataAvailable() {
        /* File villages = new File(getDataDirectory() + "/" + "village.txt.gz");
        File tribes = new File(getDataDirectory() + "/" + "tribe.txt.gz");
        File allys = new File(getDataDirectory() + "/" + "ally.txt.gz");*/
        File villages = new File(getDataDirectory() + "/" + "village.bin");
        File tribes = new File(getDataDirectory() + "/" + "tribe.bin");
        File allys = new File(getDataDirectory() + "/" + "ally.bin");

        File units = new File(getDataDirectory() + "/" + "units.xml");
        File buildings = new File(getDataDirectory() + "/" + "buildings.xml");
        File settings = new File(getDataDirectory() + "/" + "settings.xml");

        return (villages.exists() && tribes.exists() && allys.exists() && units.exists() && buildings.exists() && settings.exists());
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
                    String sURL = ServerList.getServerURL(GlobalOptions.getSelectedServer());
                    new File(GlobalOptions.getDataHolder().getDataDirectory()).mkdirs();
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
        bAborted = false;
        String serverID = GlobalOptions.getSelectedServer();
        try {
            if (pReload) {
                //completely reload data
                fireDataHolderEvents("Daten werden heruntergeladen...");
                //try to download
                if (!downloadData()) {
                    fireDataHolderEvents("Download abgebrochen/fehlgeschlagen!");
                    return false;
                }
            } else {
                //check if local loading could work
                if (!isDataAvailable()) {
                    logger.error("Local data not available. Try to download data");
                    fireDataHolderEvents("Lokale Kopie nicht gefinden. Lade Daten vom Server");
                    if (!downloadData()) {
                        logger.fatal("Download failed. No data available at the moment");
                        fireDataHolderEvents("Download abgebrochen/fehlgeschlagen");
                        return false;
                    }
                } else if (!serverSupported()) {
                    logger.error("Local data available but server not supported");
                    return false;
                } else {
                    //load data from local copy
                    fireDataHolderEvents("Lade lokale Kopie");
                    if (!readLocalDataCopy(sServerBaseDir + "/" + serverID)) {
                        logger.error("Failed to read local copy from " + sServerBaseDir + "/" + serverID);
                        return false;
                    }
                }
            }

            // <editor-fold defaultstate="collapsed" desc="DEPRECATED">
/*
            
            String line = "";
            fireDataHolderEvents("Lese Dörferliste...");
            //read villages
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/village.txt.gz"))));
            
            while ((line = reader.readLine()) != null) {
            try {
            parseVillage(line);
            } catch (Exception e) {
            //ignored (should only occur on single villages)
            }
            }
            
            try {
            reader.close();
            } catch (Exception ignored) {
            }
            fireDataHolderEvents("Lese Stämmeliste...");
            //read allies
            
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/ally.txt.gz"))));
            
            while ((line = reader.readLine()) != null) {
            try {
            parseAlly(line);
            } catch (Exception e) {
            //ignored (should only occur on single allies)
            }
            }
            
            fireDataHolderEvents("Lese Spielerliste...");
            //read tribes
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/tribe.txt.gz"))));
            while ((line = reader.readLine()) != null) {
            try {
            parseTribe(line);
            } catch (Exception e) {
            //ignored (should only occur on single tribes)
            }
            }
            
            
            try {
            reader.close();
            } catch (Exception ignored) {
            }
            fireDataHolderEvents("Lese besiegte Gegner (Angriff)...");
            //read conquers off
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_att.txt.gz"))));
            while ((line = reader.readLine()) != null) {
            try {
            parseConqueredLine(line, ID_ATT);
            } catch (Exception e) {
            //ignored (should only occur on single lines)
            }
            }
            try {
            reader.close();
            } catch (Exception ignored) {
            }
            
            fireDataHolderEvents("Lese besiegte Gegner (Verteidigung)...");
            //read conquers def
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_def.txt.gz"))));
            while ((line = reader.readLine()) != null) {
            try {
            // Tribe t = parseTribe(line);
            parseConqueredLine(line, ID_DEF);
            } catch (Exception e) {
            //ignored (should only occur on single lines)
            }
            }
            try {
            reader.close();
            } catch (Exception ignored) {
            }
             */
            //</editor-fold>

            fireDataHolderEvents("Kombiniere Daten...");
            mergeData();
            fireDataHolderEvents("Lese Servereinstellungen...");
            parseUnits();
            parseBuildings();
            fireDataHolderEvents("Daten erfolgreich gelesen");
            if (!isDataAvailable()) {
                fireDataHolderEvents("Erstelle lokale Kopie");
                if (createLocalDataCopy(sServerBaseDir + "/" + serverID)) {
                    fireDataHolderEvents("Daten erfolgreich geladen");
                } else {
                    fireDataHolderEvents("Fehler beim Erstellen der lokale Kopie");
                }
            }
        } catch (Exception e) {
            fireDataHolderEvents("Fehler beim Lesen der Daten.");
            logger.error("Failed to read server data", e);
            if (bAborted) {
                fireDataLoadedEvents();
                return false;
            }
        }

        fireDataLoadedEvents();
        return true;
    }

    private boolean createLocalDataCopy(String pServerDir) {
        try {
            Enumeration<Integer> e = mVillagesTable.keys();
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(pServerDir + "/village.bin"));
            while (e.hasMoreElements()) {
                oout.writeObject(mVillagesTable.get(e.nextElement()));
            }

            e = mTribes.keys();
            oout = new ObjectOutputStream(new FileOutputStream(pServerDir + "/tribe.bin"));
            while (e.hasMoreElements()) {
                oout.writeObject(mTribes.get(e.nextElement()));
            }

            e = mAllies.keys();
            oout = new ObjectOutputStream(new FileOutputStream(pServerDir + "/ally.bin"));
            while (e.hasMoreElements()) {
                oout.writeObject(mAllies.get(e.nextElement()));
            }
        } catch (Exception e) {
            logger.error("Failed to store local copy", e);
            return false;
        }
        return true;
    }

    public boolean readLocalDataCopy(String pServerDir) {
        try {
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(pServerDir + "/village.bin"));
            while (true) {
                try {
                    Village v = (Village) oin.readObject();
                    if (v != null) {
                        mVillages[v.getX()][v.getY()] = v;
                    }
                } catch (EOFException oefe) {
                    break;
                }
            }

            oin = new ObjectInputStream(new FileInputStream(pServerDir + "/tribe.bin"));
            while (true) {
                try {
                    Tribe t = (Tribe) oin.readObject();
                    mTribes.put(t.getId(), t);
                } catch (EOFException oefe) {
                    break;
                }
            }

            oin = new ObjectInputStream(new FileInputStream(pServerDir + "/ally.bin"));
            while (true) {
                try {
                    Ally a = (Ally) oin.readObject();
                    mAllies.put(a.getId(), a);
                } catch (EOFException oefe) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read local copy", e);
            return false;
        }
        return true;
    }

    /**Download all needed data files (villages, tribes, allies, kills, settings)*/
    private boolean downloadData() {
        URL file = null;
        String serverID = GlobalOptions.getSelectedServer();
        String serverDir = sServerBaseDir + "/" + serverID;
        new File(serverDir).mkdirs();

        try {
            // <editor-fold defaultstate="collapsed" desc="Account check">
            //check account
            String accountName = GlobalOptions.getProperty("account.name");
            String accountPassword = GlobalOptions.getProperty("account.password");
            if ((accountName == null) || (accountPassword == null)) {
                logger.error("No account name or password set");
                return false;
            }
            if (DatabaseAdapter.checkUser(accountName, accountPassword) != DatabaseAdapter.ID_SUCCESS) {
                logger.error("Failed to validate account (Wrong username or password?)");
                return false;
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Version check">
            if (DatabaseAdapter.isVersionAllowed() != DatabaseAdapter.ID_SUCCESS) {
                logger.error("Current version is not allowed any longer");
                fireDataHolderEvents("Deine DS-Workbench Version ist zu alt. Bitte lade dir die aktuelle Version herunter.");
                return false;
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Server settings check">
            //download settings.xml
            String sURL = ServerList.getServerURL(GlobalOptions.getSelectedServer());

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

            int dataVersion = DatabaseAdapter.getDataVersion(serverID);
            int userDataVersion = DatabaseAdapter.getUserDataVersion(accountName, serverID);
            int maxDiff = Integer.parseInt(DatabaseAdapter.getPropertyValue("max_user_diff"));
            String downloadURL = DatabaseAdapter.getServerDownloadURL(serverID);
            if ((userDataVersion == dataVersion)) {
                //no update needed
                return true;
            } else if ((userDataVersion == -666) || (dataVersion - userDataVersion > maxDiff)) {
                //full download if no download made yet or diff too large
                //load villages
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
                //load tribes
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
                //load allies
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

                //load conquers off
                fireDataHolderEvents("Lese besiegte Gegner (Angriff)...");
                u = new URL(downloadURL + "/kill_att.txt.gz");
                r = new BufferedReader(new InputStreamReader(u.openConnection().getInputStream()));
                line = "";
                while ((line = r.readLine()) != null) {
                    try {
                        parseConqueredLine(line, ID_ATT);
                    } catch (Exception e) {
                        //ignored (should only occur on single lines)
                    }
                }
                r.close();

                //read conquers def
                fireDataHolderEvents("Lese besiegte Gegner (Verteidigung)...");
                u = new URL(downloadURL + "/kill_def.txt.gz");
                r = new BufferedReader(new InputStreamReader(u.openConnection().getInputStream()));
                line = "";
                while ((line = r.readLine()) != null) {
                    try {
                        parseConqueredLine(line, ID_DEF);
                    } catch (Exception e) {
                        //ignored (should only occur on single lines)
                    }
                }
                r.close();
                //finally register user for server if not done
                if (userDataVersion == -666) {
                    DatabaseAdapter.registerUserForServer(accountName, serverID);
                }
            } else {
                //normal update of all diffs
                for (int i = userDataVersion; i <= dataVersion; i++) {
                    //download every diff between user version and server version
                }
            }

            // <editor-fold defaultstate="collapsed" desc="DEPRECATED">

            /*
            fireDataHolderEvents("Lade village.txt.gz");
            file = new URL(sURL.toString() + "/map/village.txt.gz");
            downloadDataFile(file, "village_tmp.txt.gz");
            target = new File(serverDir + "/village.txt.gz");
            if (target.exists()) {
            target.delete();
            }
            new File("village_tmp.txt.gz").renameTo(target);
            
            //download tribe.txt
            fireDataHolderEvents("Lade tribe.txt.gz");
            file = new URL(sURL.toString() + "/map/tribe.txt.gz");
            downloadDataFile(file, "tribe_tmp.txt.gz");
            target = new File(serverDir + "/tribe.txt.gz");
            if (target.exists()) {
            target.delete();
            }
            new File("tribe_tmp.txt.gz").renameTo(target);
            
            //download ally.txt
            fireDataHolderEvents("Lade ally.txt.gz");
            file = new URL(sURL.toString() + "/map/ally.txt.gz");
            downloadDataFile(file, "ally_tmp.txt.gz");
            target = new File(serverDir + "/ally.txt.gz");
            if (target.exists()) {
            target.delete();
            }
            new File("ally_tmp.txt.gz").renameTo(target);
            
            //download kill_att.txt
            fireDataHolderEvents("Lade kill_att.txt.gz");
            file = new URL(sURL.toString() + "/map/kill_att.txt.gz");
            downloadDataFile(file, "kill_att_tmp.txt.gz");
            target = new File(serverDir + "/kill_att.txt.gz");
            if (target.exists()) {
            target.delete();
            }
            new File("kill_att_tmp.txt.gz").renameTo(target);
            
            //download kill_def.txt
            fireDataHolderEvents("Lade kill_def.txt.gz");
            file = new URL(sURL.toString() + "/map/kill_def.txt.gz");
            downloadDataFile(file, "kill_def_tmp.txt.gz");
            target = new File(serverDir + "/kill_def.txt.gz");
            if (target.exists()) {
            target.delete();
            }
            new File("kill_def_tmp.txt.gz").renameTo(target);
             */
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Direct download from DS-Servers">
            //download unit information, but only once
            target = new File(serverDir + "/units.xml");
            if (!target.exists()) {
                fireDataHolderEvents("Lade Information über Einheiten");
                file = new URL(sURL + "/interface.php?func=get_unit_info");
                downloadDataFile(file, "units_tmp.xml");

                new File("units_tmp.xml").renameTo(target);
            }

            //download building information, but only once
            target = new File(serverDir + "/buildings.xml");
            if (!target.exists()) {
                fireDataHolderEvents("Lade Information über Gebäude");
                file = new URL(sURL + "/interface.php?func=get_building_info");
                downloadDataFile(file, "buildings_tmp.xml");
                new File("buildings_tmp.xml").renameTo(target);
            }
            //</editor-fold>

            DatabaseAdapter.updateUserDataVersion(accountName, serverID, dataVersion);

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
                Village current = mVillages[i][j];
                if (current != null) {
                    mVillagesTable.put(current.getId(), current);
                    Tribe t = mTribes.get(current.getTribeID());
                    current.setTribe(t);
                    if (t != null) {
                        t.addVillage(current);
                        Ally currentAlly = mAllies.get(t.getAllyID());
                        t.setAlly(currentAlly);
                        if (currentAlly != null) {
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
        //BufferedReader reader = new BufferedReader(new InputStreamReader(ucon.getInputStream()));
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
        String buildingsFile = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
        buildingsFile +=
                "/units.xml";
        try {
            Document d = JaxenUtils.getDocument(new File(buildingsFile));
            d =
                    JaxenUtils.getDocument(new File(buildingsFile));
            List<Element> l = JaxenUtils.getNodes(d, "/config/*");
            for (Element e : l) {
                try {
                    mUnits.add(new UnitHolder(e));
                } catch (Exception inner) {
                }
            }
        } catch (Exception outer) {
            logger.error("Failed to load units", outer);
            fireDataHolderEvents("Laden der Einheiten fehlgeschlagen");
        }

    }

    /**Parse the list of buildings*/
    public void parseBuildings() {
        String buildingsFile = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
        buildingsFile +=
                "/buildings.xml";
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

    public Village[][] getVillages() {
        return mVillages;
    }

    public Hashtable<Integer, Village> getVillagesById() {
        return mVillagesTable;
    }

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

    public List<UnitHolder> getUnits() {
        return mUnits;
    }

    public UnitHolder getUnitByPlainName(String pName) {
        for (UnitHolder u : getUnits()) {
            if (u.getPlainName().equals(pName)) {
                return u;
            }
        }
        return null;
    }

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

    public List<BuildingHolder> getBuildings() {
        return mBuildings;
    }

    public synchronized void fireDataHolderEvents(String pMessage) {
        for (DataHolderListener listener : mListeners) {
            listener.fireDataHolderEvent(pMessage);
        }
    }

    public synchronized void fireDataLoadedEvents() {
        for (DataHolderListener listener : mListeners) {
            listener.fireDataLoadedEvent();
        }
    }
}
