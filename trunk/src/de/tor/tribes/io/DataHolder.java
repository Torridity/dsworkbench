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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public final static int MAX_AGE = 24 * 60 * 60 * 1000;
    private final int ID_ATT = 0;
    private final int ID_DEF = 1;
    private Village[][] mVillages = null;
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
                System.out.println("Dir " + serverDir.getName());
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
        File villages = new File(getDataDirectory() + "/" + "village.txt.gz");
        File tribes = new File(getDataDirectory() + "/" + "tribe.txt.gz");
        File allys = new File(getDataDirectory() + "/" + "ally.txt.gz");
        File killsOff = new File(getDataDirectory() + "/" + "kill_att.txt.gz");
        File killsDef = new File(getDataDirectory() + "/" + "kill_def.txt.gz");
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
                    URL sURL = ServerList.getServerURL(GlobalOptions.getSelectedServer());
                    new File(GlobalOptions.getDataHolder().getDataDirectory()).mkdirs();
                    fireDataHolderEvents("Lese Server Einstellungen");
                    URL file = new URL(sURL.toString() + "/interface.php?func=get_config");
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
                    logger.error("Local data brocken. Try to download data");
                    fireDataHolderEvents("Lokal gespeicherte Daten sind fehlerhaft. Versuche erneuten Download");
                    if (!downloadData()) {
                        logger.fatal("Download failed. No data available at the moment");
                        fireDataHolderEvents("Download abgebrochen/fehlgeschlagen");
                        return false;
                    }
                } else if (!serverSupported()) {
                    logger.error("Local data available but server not supported");
                    return false;
                }
            }

            String line = "";
            int bytes = 0;
            fireDataHolderEvents("Lese Dörferliste...");
            //read villages
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/village.txt.gz"))));

            while ((line = reader.readLine()) != null) {
                bytes += line.length();
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
                bytes += line.length();
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
                bytes += line.length();
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
            //read tribes
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_att.txt.gz"))));
            while ((line = reader.readLine()) != null) {
                bytes += line.length();
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
            //read tribes
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(getDataDirectory() + "/kill_def.txt.gz"))));
            while ((line = reader.readLine()) != null) {
                bytes += line.length();
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

            fireDataHolderEvents("Kombiniere Daten...");
            mergeData();
            fireDataHolderEvents("Lese Servereinstellungen...");
            parseUnits();
            parseBuildings();
            fireDataHolderEvents("Daten erfolgreich gelesen");
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

    /**Download all needed data files (villages, tribes, allies, kills, settings)*/
    private boolean downloadData() {
        URL file = null;
        String serverDir = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
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

            if (DatabaseAdapter.isUpdatePossible(accountName, GlobalOptions.getSelectedServer())) {
                logger.info("Update possible, try starting download");
            } else {
                logger.error("Download not yet possible");
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
            URL sURL = ServerList.getServerURL(GlobalOptions.getSelectedServer());

            fireDataHolderEvents("Lese Server Einstellungen");
            File target = new File(serverDir + "/settings.xml");
            if (!target.exists()) {
                file = new URL(sURL.toString() + "/interface.php?func=get_config");
                downloadDataFile(file, "settings_tmp.xml");
                new File("settings_tmp.xml").renameTo(target);
            }

            if (!serverSupported()) {
                return false;
            }
            //</editor-fold>

            /*
            //villages download/merge
            File localFile = new File(getDataDirectory() + "/" + "village.txt.gz");
            fireDataHolderEvents("Lade village.txt.gz");
            if (localFile.exists()) {
            //download diff
            downloadDiff("village");
            } else {
            //download full
            downloadFull("village");
            }
            
            //tribe download/merge
            localFile = new File(getDataDirectory() + "/" + "tribe.txt.gz");
            
            if (localFile.exists()) {
            //download diff
            fireDataHolderEvents("Lade tribe.diff");
            downloadDiff("tribe");
            } else {
            //download full
            fireDataHolderEvents("Lade tribe.txt.gz");
            downloadFull("tribe");
            }
            
            //ally download/merge
            localFile = new File(getDataDirectory() + "/" + "ally.txt.gz");
            if (localFile.exists()) {
            //download diff
            fireDataHolderEvents("Lade ally.diff");
            downloadDiff("ally");
            } else {
            //download full
            fireDataHolderEvents("Lade ally.txt.gz");
            downloadFull("ally");
            }
            
            //kill_att download/merge
            localFile = new File(getDataDirectory() + "/" + "kill_att.txt.gz");
            
            if (localFile.exists()) {
            //download diff
            fireDataHolderEvents("Lade kill_att.diff");
            downloadDiff("kill_att");
            } else {
            //download full
            fireDataHolderEvents("Lade kill_att.txt.gz");
            downloadFull("kill_att");
            }
            
            //kill_def download/merge
            localFile = new File(getDataDirectory() + "/" + "kill_def.txt.gz");
            fireDataHolderEvents("Lade kill_def.txt.gz");
            if (localFile.exists()) {
            //download diff
            fireDataHolderEvents("Lade kill_def.diff");
            downloadDiff("kill_def");
            } else {
            //download full
            fireDataHolderEvents("Lade kill_def.txt.gz");
            downloadFull("kill_def");
            }
            
            sURL = new URL("http://www.torridity.de/servers/" + GlobalOptions.getSelectedServer() + "/village.diff");
             */
            
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

            // <editor-fold defaultstate="collapsed" desc="Direct download from DS-Servers">
            //download unit information, but only once
            target = new File(serverDir + "/units.xml");
            if (!target.exists()) {
                fireDataHolderEvents("Lade Information über Einheiten");
                file = new URL(sURL.toString() + "/interface.php?func=get_unit_info");
                downloadDataFile(file, "units_tmp.xml");

                new File("units_tmp.xml").renameTo(target);
            }

            //download building information, but only once
            target = new File(serverDir + "/buildings.xml");
            if (!target.exists()) {
                fireDataHolderEvents("Lade Information über Gebäude");
                file = new URL(sURL.toString() + "/interface.php?func=get_building_info");
                downloadDataFile(file, "buildings_tmp.xml");
                new File("buildings_tmp.xml").renameTo(target);
            }
            //</editor-fold>

            fireDataHolderEvents("Download erfolgreich beendet.");
            DatabaseAdapter.storeLastUpdate(accountName, GlobalOptions.getSelectedServer());
        } catch (Exception e) {
            fireDataHolderEvents("Download fehlgeschlagen.");
            logger.error("Failed to download data", e);
            return false;
        }
        return true;
    }

    private void downloadDiff(String pFile) throws Exception {
        String localFile = getDataDirectory() + "/" + pFile + ".txt.gz";
        URL remoteDiffFile = new URL("http://www.torridity.de/servers/" + GlobalOptions.getSelectedServer() + "/" + pFile + ".diff");
        InputStream diffIn = null;
        try {
            diffIn = remoteDiffFile.openStream();
        } catch (FileNotFoundException fnf) {
            //no diff available, perform full update
            logger.info("No differential update available. Doing full update");
            downloadFull(pFile);
            return;
        }

        BufferedReader diffReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(diffIn)));
        BufferedReader localReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(localFile))));
        String diffLine = "";

        StringBuffer merged = new StringBuffer();
        while ((diffLine = diffReader.readLine()) != null) {
            String localLine = localReader.readLine();
            if (localLine != null) {
                //read all diff content
                if (diffLine.trim().length() == 0) {
                    //no change
                    merged.append(localLine + "\n");
                } else {
                    StringTokenizer diffTokens = new StringTokenizer(diffLine, ",");
                    StringTokenizer localTokens = new StringTokenizer(localLine, ",");
                    while (diffTokens.hasMoreTokens()) {
                        //loop over all tokens
                        String diffToken = diffTokens.nextToken();
                        if (diffToken.trim().length() == 0) {
                            //diff token is empty, no change
                            if (diffTokens.hasMoreTokens()) {
                                merged.append(localTokens.nextToken() + ",");
                            } else {
                                merged.append(localTokens.nextToken().trim());
                            }
                        } else {
                            //diff token is not empty, value has changed
                            if (diffTokens.hasMoreTokens()) {
                                merged.append(diffToken + ",");
                            } else {
                                merged.append(diffToken.trim());
                            }
                            //drop the local token
                            localTokens.nextToken();
                        }
                    }
                    merged.append("\n");
                }
            } else {
                merged.append(diffLine + "\n");
            }
        }
        try {
            localReader.close();
            diffReader.close();
        } catch (Exception e) {
        }
        logger.debug("Writing merged data");
        GZIPOutputStream mergedOut = new GZIPOutputStream(new FileOutputStream(localFile));
        mergedOut.write(merged.toString().getBytes());
        mergedOut.finish();
        mergedOut.close();
    }

    private void downloadFull(String pFile) throws Exception {
        String localFile = getDataDirectory() + "/" + pFile + ".txt.gz";
        URL remoteFile = new URL("http://www.torridity.de/servers/" + GlobalOptions.getSelectedServer() + "/" + pFile + ".txt.gz");
        BufferedReader remoteReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(remoteFile.openStream())));

        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = remoteReader.readLine()) != null) {
            buffer.append(line + "\n");
        }
        logger.debug("Writing  data");
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(localFile));
        out.write(buffer.toString().getBytes());
        out.finish();
        out.close();
    }

    /**Merge all data into the village data structure to ease searching*/
    private void mergeData() {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                Village current = mVillages[i][j];
                if (current != null) {
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

    /**Parse a village*/
    private void parseVillage(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");

        Village entry = new Village();
        if (tokenizer.countTokens() < 7) {
            return;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            String name = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
            //replace HTML characters
            name = name.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
            entry.setName(name);
            entry.setX(Short.parseShort(tokenizer.nextToken()));
            entry.setY(Short.parseShort(tokenizer.nextToken()));
            entry.setTribeID(Integer.parseInt(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setType(Byte.parseByte(tokenizer.nextToken()));
            mVillages[entry.getX()][entry.getY()] = entry;
        } catch (Exception e) {
            //village invalid
        }
    }

    /**Parse an ally*/
    private void parseAlly(String line) {
        //$id, $name, $tag, $members, $villages, $points, $all_points, $rank
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        Ally entry = new Ally();
        if (tokenizer.countTokens() < 8) {
            return;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            entry.setName(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setTag(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setMembers(Short.parseShort(tokenizer.nextToken()));
            entry.setVillages(Integer.parseInt(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setAll_points(Integer.parseInt(tokenizer.nextToken()));
            entry.setRank(Integer.parseInt(tokenizer.nextToken()));
            mAllies.put(entry.getId(), entry);
        } catch (Exception e) {
            //ally entry invalid
        }
    }

    /**Parse a tribe*/
    private void parseTribe(String line) {
        //$id, $name, $ally, $villages, $points, $rank
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        Tribe entry = new Tribe();
        if (tokenizer.countTokens() < 6) {
            return;
        }

        try {
            entry.setId(Integer.parseInt(tokenizer.nextToken()));
            entry.setName(URLDecoder.decode(tokenizer.nextToken(), "UTF-8"));
            entry.setAllyID(Integer.parseInt(tokenizer.nextToken()));
            entry.setVillages(Short.parseShort(tokenizer.nextToken()));
            entry.setPoints(Integer.parseInt(tokenizer.nextToken()));
            entry.setRank(Integer.parseInt(tokenizer.nextToken()));
            mTribes.put(entry.getId(), entry);
        } catch (Exception e) {
            //tribe entry invalid
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
            d =
                    JaxenUtils.getDocument(new File(buildingsFile));
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
