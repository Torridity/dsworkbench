/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchTroopsFrame;
import de.tor.tribes.ui.models.TroopsManagerTableModel;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Charon
 */
public class TroopsManager {

    private static Logger logger = Logger.getLogger("TroopsManager");
    private static TroopsManager SINGLETON = null;
    private Hashtable<Village, VillageTroopsHolder> mTroops = null;
    private List<TroopsManagerListener> mManagerListeners = null;
    private List<Image> mTroopMarkImages = new LinkedList<Image>();

    public static synchronized TroopsManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TroopsManager();
        }
        return SINGLETON;
    }

    TroopsManager() {
        mTroops = new Hashtable<Village, VillageTroopsHolder>();
        mManagerListeners = new LinkedList<TroopsManagerListener>();
        try {
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/off_marker.png")));
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/def_marker.png")));
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/def_cav_marker.png")));
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/def_arch_marker.png")));
        } catch (Exception e) {
            logger.error("Failed to read troops markers", e);
        }
    }

    public synchronized void addTroopsManagerListener(TroopsManagerListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mManagerListeners.contains(pListener)) {
            mManagerListeners.add(pListener);
        }
    }

    public synchronized void removeTroopsManagerListener(TroopsManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    public void loadTroopsFromDatabase(String pUrl) {
        //not yet implemented
    }

    public void addTroopsForVillage(Village pVillage, List<Integer> pTroops) {
        addTroopsForVillage(pVillage, Calendar.getInstance().getTime(), pTroops);
        fireTroopsChangedEvents();
    }

    public void addTroopsForVillageFast(Village pVillage, List<Integer> pTroops) {
        addTroopsForVillageFast(pVillage, Calendar.getInstance().getTime(), pTroops);
    }

    public void addTroopsForVillage(Village pVillage, Date pState, List<Integer> pTroops) {
        mTroops.put(pVillage, new VillageTroopsHolder(pVillage, pState));
        fireTroopsChangedEvents();
    }

    public void addTroopsForVillageFast(Village pVillage, Date pState, List<Integer> pTroops) {
        mTroops.put(pVillage, new VillageTroopsHolder(pVillage, pState));
    }

    public VillageTroopsHolder getTroopsForVillage(Village pVillage) {
        return mTroops.get(pVillage);
    }

    public void removeTroopsForVillage(Village pVillage) {
        mTroops.remove(pVillage);
        fireTroopsChangedEvents();
    }

    public Image getTroopsMarkerForVillage(Village pVillage) {
        VillageTroopsHolder holder = getTroopsForVillage(pVillage);
        if (holder == null) {
            return null;
        }
        List<Double> l = new LinkedList<Double>();
        double off = holder.getOffValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double def = holder.getDefValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double defCav = holder.getDefCavalryValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);
        double defArch = holder.getDefArcherValue(TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE);

        l.add(off);
        l.add(def);
        l.add(defCav);
        l.add(defArch);

        Collections.sort(l);

        double max = l.get(3);
        if (max == off) {
            return mTroopMarkImages.get(0);
        } else if (max == def) {
            return mTroopMarkImages.get(1);
        } else if (max == defCav) {
            return mTroopMarkImages.get(2);
        }
        //archer def must be max.
        return mTroopMarkImages.get(3);
    }

    public int getEntryCount() {
        return mTroops.size();
    }

    public Village[] getVillages() {
        return mTroops.keySet().toArray(new Village[]{});
    }

    public void loadTroopsFromFile(String pFile) {
        mTroops.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        File troopsFile = new File(pFile);
        if (troopsFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.info("Loading troops from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(troopsFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//villages/village")) {
                    /*        //get basic village without merged information
                    Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText()));
                    Date state = Calendar.getInstance().getTime();
                    try {
                    state = new Date(Long.parseLong(e.getChild("state").getText()));
                    } catch (Exception ie) {
                    }
                    //get correct village
                    v = DataHolder.getSingleton().getVillages()[v.getX()][v.getY()];
                    List<Integer> troops = new LinkedList<Integer>();
                    for (Element t : (List<Element>) JaxenUtils.getNodes(e, "troops/troop")) {
                    troops.add(Integer.parseInt(t.getText()));
                    }
                    mTroops.put(v, new VillageTroopsHolder(v, troops, state));*/
                    VillageTroopsHolder holder = VillageTroopsHolder.fromXml(e);
                    mTroops.put(holder.getVillage(), holder);
                }
                logger.debug("Troops loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load troops", e);
            }
        } else {
            logger.info("No troops found under '" + pFile + "'");
        }
    }

    public boolean importTroops(File pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }

        logger.info("Importing troops");

        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//troops/villages/village")) {
                //get basic village without merged information
              /*  Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText()));
                Date state = Calendar.getInstance().getTime();
                try {
                state = new Date(Long.parseLong(e.getChild("state").getText()));
                } catch (Exception ie) {
                }
                //get correct village
                v = DataHolder.getSingleton().getVillages()[v.getX()][v.getY()];
                List<Integer> troops = new LinkedList<Integer>();
                for (Element t : (List<Element>) JaxenUtils.getNodes(e, "troops/troop")) {
                troops.add(Integer.parseInt(t.getText()));
                }
                mTroops.put(v, new VillageTroopsHolder(v, state));*/
                VillageTroopsHolder holder = VillageTroopsHolder.fromXml(e);
                mTroops.put(holder.getVillage(), holder);
            }
            logger.debug("Troops imported successfully");
            DSWorkbenchTroopsFrame.getSingleton().fireTroopsChangedEvent();
            return true;
        } catch (Exception e) {
            logger.error("Failed to import troops", e);
            DSWorkbenchTroopsFrame.getSingleton().fireTroopsChangedEvent();
            return false;
        }
    }

    public String getExportData() {
        try {
            logger.debug("Generating troop export data");

            String result = "<troops>\n<villages>\n";
            Enumeration<Village> villages = mTroops.keys();
            while (villages.hasMoreElements()) {
                //write village information
                Village v = villages.nextElement();
                VillageTroopsHolder holder = mTroops.get(v);
                /* result += "<village>\n";
                result += "<id>" + v.getId() + "</id>\n";
                result += "<state>" + holder.getState().getTime() + "</state>\n";
                result += "<troops>\n";
                for (Integer i : holder.getTroops()) {
                //write troop information
                result += "<troop>" + i + "</troop>\n";
                }
                //close troops for village
                result += "</troops>\n";
                result += "</village>\n";*/
                result += holder.toXml() + "\n";
            }
            result += "</villages>\n</troops>\n";
            logger.debug("Export data generated successfully");
            return result;
        } catch (Exception e) {
            logger.error("Failed to generate troop export data", e);
            return "";
        }
    }

    public void saveTroopsToDatabase(String pUrl) {
        //not implemented yet
    }

    public void saveTroopsToFile(String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<villages>\n");
            Enumeration<Village> villages = mTroops.keys();
            while (villages.hasMoreElements()) {
                /*        //write village information
                Village v = villages.nextElement();
                VillageTroopsHolder holder = mTroops.get(v);
                w.write("<village>\n");
                w.write("<id>" + v.getId() + "</id>\n");
                w.write("<state>" + holder.getState().getTime() + "</state>\n");
                w.write("<troops>\n");
                for (Integer i : holder.getTroops()) {
                //write troop information
                w.write("<troop>" + i + "</troop>\n");
                }
                //close troops for village
                w.write("</troops>\n");
                w.write("</village>\n");*/

                Village v = villages.nextElement();
                VillageTroopsHolder holder = mTroops.get(v);
                w.write(holder.toXml());

            }
            //close all
            w.write("</villages>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store troops", e);
        }
    }

    public void forceUpdate() {
        fireTroopsChangedEvents();
    }

    /**Notify attack manager listeners about changes*/
    private void fireTroopsChangedEvents() {
        TroopsManagerListener[] listeners = mManagerListeners.toArray(new TroopsManagerListener[]{});
        for (TroopsManagerListener listener : listeners) {
            listener.fireTroopsChangedEvent();
        }
    }
}
