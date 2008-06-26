/*
 * AbstractDataReader.java
 *
 * Created on 17.07.2007, 21:49:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.tor.tribes.io;

import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
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
    private Village[][] mVillages = null;
    private Hashtable<Integer, Ally> mAllies = null;
    private Hashtable<Integer, Tribe> mTribes = null;
    private List<BuildingHolder> mBuildings = null;
    private List<UnitHolder> mUnits = null;
    private DataHolderListener mListener = null;
    private boolean bAborted = false;
    private String sSelectedServer = null;
    private String sServerBaseDir = "./servers";

    public DataHolder(DataHolderListener pListener) {
        mListener = pListener;
        mVillages = new Village[1000][1000];
        mAllies = new Hashtable<Integer, Ally>();
        mTribes = new Hashtable<Integer, Tribe>();
        mBuildings = new LinkedList<BuildingHolder>();
        mUnits = new LinkedList<UnitHolder>();
        new File(sServerBaseDir).mkdir();
    }

    public String getDataDirectory() {
        return sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
    }

    private void abort() {
        bAborted = true;
    }

    public boolean loadData(boolean pReload) {
        bAborted = false;
        try {
            String serverDir = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
            if (pReload || !isDataAvailable()) {
                fireDataHolderEvents("Daten werden heruntergeladen...");
                if (!downloadData()) {
                    fireDataHolderEvents("Download fehlgeschlagen!");
                    return false;
                }
            }
            fireDataHolderEvents("Prüfe Server Einstellungen");
            Document d = JaxenUtils.getDocument(new File(serverDir + "/settings.xml"));
            try {
                Integer mapType = Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/sector"));
                if (mapType != 2) {
                    fireDataHolderEvents("Der gewählte Sever wird leider (noch) nicht unterstützt");
                    logger.error("Map type '" + mapType + "' is not supported yet");
                    return false;
                }
            } catch (Exception e) {
                logger.error("Failed to check server settings", e);
                fireDataHolderEvents("Der gewählte Sever wird leider (noch) nicht unterstützt");
                return false;
            }

            String line = "";
            int bytes = 0;
            fireDataHolderEvents("Lese Dörferliste...");
            //read villages
            BufferedReader reader = new BufferedReader(new FileReader(serverDir + "/village.txt"));

            while ((line = reader.readLine()) != null) {
                bytes += line.length();
                try {
                    Village v = parseVillage(line);
                    if (bAborted) {
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("Error while reading village " + line);
                    e.printStackTrace();
                    fireDataHolderEvents("Fehler beim Lesen der Dörferliste!");
                }
            }

            fireDataHolderEvents("Lese Stämmeliste...");
            //read allies
            reader = new BufferedReader(new FileReader(serverDir + "/ally.txt"));

            while ((line = reader.readLine()) != null) {
                bytes += line.length();
                try {
                    Ally a = parseAlly(line);
                    if (bAborted) {
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("Error while reading ally " + line);
                    fireDataHolderEvents("Fehler beim Lesen der Stämmeliste!");
                    e.printStackTrace();
                }
            }

            fireDataHolderEvents("Lese Spielerliste...");
            //read tribes
            reader = new BufferedReader(new FileReader(serverDir + "/tribe.txt"));

            while ((line = reader.readLine()) != null) {
                bytes += line.length();
                try {
                    Tribe t = parseTribe(line);
                    if (bAborted) {
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("Error while reading tribe " + line);
                    fireDataHolderEvents("Fehler beim Lesen der Spielerliste!");
                    e.printStackTrace();
                }
            }

            fireDataHolderEvents("Kombiniere Daten...");
            mergeData();
            fireDataHolderEvents("Lese Servereinstellungen...");
            parseUnits();
            parseBuildings();
            fireDataHolderEvents("Daten erfolgreich gelesen.");
        } catch (Exception e) {
            fireDataHolderEvents("Fehler beim Lesen der Daten.");
            e.printStackTrace();
            if (bAborted) {
                if (mListener != null) {
                    mListener.fireDataLoadedEvent();
                }
                return false;
            }
        }
        if (mListener != null) {
            mListener.fireDataLoadedEvent();
        }
        return true;
    }

    private void mergeData() {
        Enumeration<Integer> tribes = mTribes.keys();
        while (tribes.hasMoreElements()) {
            Tribe current = mTribes.get(tribes.nextElement());
            Ally currentAlly = mAllies.get(current.getAllyID());
            if (currentAlly != null) {
                currentAlly.addTribe(current);
            }
            current.setAlly(currentAlly);
        }
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                Village current = mVillages[i][j];
                if (current != null) {
                    Tribe t = mTribes.get(current.getTribeID());
                    current.setTribe(t);
                    if (t != null) {
                        t.addVillage(current);
                    }
                }
            }
        }
    }

    private boolean isDataAvailable() {
        String serverDir = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
        File villages = new File(serverDir + "/" + "village.txt");
        File tribes = new File(serverDir + "/" + "tribe.txt");
        File allys = new File(serverDir + "/" + "ally.txt");
        File units = new File(serverDir + "/" + "units.xml");
        File buildings = new File(serverDir + "/" + "buildings.xml");
        File settings = new File(serverDir + "/" + "settings.xml");
        return (villages.exists() && tribes.exists() && allys.exists() && units.exists() && buildings.exists() && settings.exists());
    }

    private boolean downloadData() {
        URL file = null;
        String serverDir = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
        new File(serverDir).mkdirs();
        try {
            //download village.txt
            URL sURL = ServerList.getServerURL(GlobalOptions.getSelectedServer());

            fireDataHolderEvents("Lese Server Einstellungen");
            File target = new File(serverDir + "/settings.xml");
            if (!target.exists()) {
                file = new URL(sURL.toString() + "/interface.php?func=get_config");
                downloadDataFile(file, "settings_tmp.xml");
                new File("settings_tmp.xml").renameTo(target);
            }

            fireDataHolderEvents("Lade village.txt");
            file = new URL(sURL.toString() + "/map/village.txt");
            downloadDataFile(file, "village_tmp.txt");
            target = new File(serverDir + "/village.txt");
            if (target.exists()) {
                target.delete();
            }
            new File("village_tmp.txt").renameTo(target);

            //download tribe.txt
            fireDataHolderEvents("Lade tribe.txt");
            file = new URL(sURL.toString() + "/map/tribe.txt");
            downloadDataFile(file, "tribe_tmp.txt");
            target = new File(serverDir + "/tribe.txt");
            if (target.exists()) {
                target.delete();
            }
            new File("tribe_tmp.txt").renameTo(target);

            //download ally.txt
            fireDataHolderEvents("Lade ally.txt");
            file = new URL(sURL.toString() + "/map/ally.txt");
            downloadDataFile(file, "ally_tmp.txt");
            target = new File(serverDir + "/ally.txt");
            if (target.exists()) {
                target.delete();
            }
            new File("ally_tmp.txt").renameTo(target);

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
            fireDataHolderEvents("Download erfolgreich beendet.");
        } catch (Exception e) {
            fireDataHolderEvents("Download fehlgeschlagen.");
            logger.error("Failed to download data", e);
            return false;
        }
        return true;
    }

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
        tempWriter.close();
    }

    private Village parseVillage(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        Village entry = new Village();
        List<String> entries = new LinkedList();
        if (tokenizer.countTokens() < 7) {
            return null;
        }
        while (tokenizer.hasMoreTokens()) {
            entries.add(tokenizer.nextToken());
        }
        entry.setId(Integer.parseInt(entries.get(0)));
        try {
            String name = URLDecoder.decode(entries.get(1), "UTF-8");
            name = name.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
            entry.setName(name);
        } catch (Exception e) {
            return null;
        }
        entry.setX(Integer.parseInt(entries.get(2)));
        entry.setY(Integer.parseInt(entries.get(3)));
        entry.setTribeID(Integer.parseInt(entries.get(4)));
        entry.setPoints(Integer.parseInt(entries.get(5)));

        //set village type on new servers
        try {
            entry.setType(Integer.parseInt(entries.get(6)));
        } catch (Exception e) {
        }
        mVillages[entry.getX()][entry.getY()] = entry;
        return entry;
    }

    private Ally parseAlly(String line) {
        //$id, $name, $tag, $members, $villages, $points, $all_points, $rank
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        Ally entry = new Ally();
        List<String> entries = new LinkedList();
        if (tokenizer.countTokens() < 8) {
            return null;
        }
        while (tokenizer.hasMoreTokens()) {
            entries.add(tokenizer.nextToken());
        }
        entry.setId(Integer.parseInt(entries.get(0)));
        try {
            entry.setName(URLDecoder.decode(entries.get(1), "UTF-8"));
            entry.setTag(URLDecoder.decode(entries.get(2), "UTF-8"));
        } catch (Exception e) {
            return null;
        }
        entry.setMembers(Integer.parseInt(entries.get(3)));
        entry.setVillages(Integer.parseInt(entries.get(4)));
        entry.setPoints(Integer.parseInt(entries.get(5)));
        entry.setAll_points(Integer.parseInt(entries.get(6)));
        entry.setRank(Integer.parseInt(entries.get(7)));
        mAllies.put(entry.getId(), entry);
        return entry;
    }

    private Tribe parseTribe(String line) {
        //$id, $name, $ally, $villages, $points, $rank
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        Tribe entry = new Tribe();
        List<String> entries = new LinkedList();
        if (tokenizer.countTokens() < 6) {
            return null;
        }
        while (tokenizer.hasMoreTokens()) {
            entries.add(tokenizer.nextToken());
        }
        entry.setId(Integer.parseInt(entries.get(0)));
        try {
            entry.setName(URLDecoder.decode(entries.get(1), "UTF-8"));
        } catch (Exception e) {
            return null;
        }
        entry.setAllyID(Integer.parseInt(entries.get(2)));
        entry.setVillages(Integer.parseInt(entries.get(3)));
        entry.setPoints(Integer.parseInt(entries.get(4)));
        entry.setRank(Integer.parseInt(entries.get(5)));
        mTribes.put(entry.getId(), entry);
        return entry;
    }

    private void parseUnits() {
        String buildingsFile = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
        buildingsFile += "/units.xml";
        try {
            Document d = JaxenUtils.getDocument(new File(buildingsFile));
            d = JaxenUtils.getDocument(new File(buildingsFile));
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

    public void parseBuildings() {
        String buildingsFile = sServerBaseDir + "/" + GlobalOptions.getSelectedServer();
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

    public Village[][] getVillages() {
        return mVillages;
    }

    public Hashtable<Integer, Ally> getAllies() {
        return mAllies;
    }

    public Hashtable<Integer, Tribe> getTribes() {
        return mTribes;
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

    public void fireDataHolderEvents(String pMessage) {
        if (mListener != null) {
            mListener.fireDataHolderEvent(pMessage);
        }
    }
}
