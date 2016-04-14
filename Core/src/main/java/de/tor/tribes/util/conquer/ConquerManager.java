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
package de.tor.tribes.util.conquer;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Charon
 */
public class ConquerManager extends GenericManager<Conquer> {

    private static Logger logger = Logger.getLogger("ConquerManager");
    private static ConquerManager SINGLETON = null;
    private long lastUpdate = -1;
    private ConquerUpdateThread updateThread = null;

    public static synchronized ConquerManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ConquerManager();
        }
        return SINGLETON;
    }

    ConquerManager() {
        super(false);
        updateThread = new ConquerUpdateThread();
        updateThread.start();
    }

    public void addConquer(Conquer c) {
        if (c != null) {
            addManagedElement(c);
        }
    }

    public int getConquerCount() {
        return getElementCount();
    }

    public Conquer getConquer(int id) {
        return (Conquer) getAllElements().get(id);
    }

    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File conquerFile = new File(pFile);
        if (conquerFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading conquers from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(conquerFile);
                String lastup = JaxenUtils.getNodeValue(d, "//conquers/lastUpdate");
                setLastUpdate(Long.parseLong(lastup));
                if (getLastUpdate() == -1) {
                    //set update correct on error
                    setLastUpdate(0);
                }
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//conquers/conquer")) {
                    try {
                        Conquer c = new Conquer();
                        c.loadFromXml(e);
                        addConquer(c);
                    } catch (Exception inner) {
                        //ignored, conquer invalid
                    }
                }
                logger.debug("Conquers successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load conquers", e);
                lastUpdate = 0;
            }

            //merge conquers and world data
            logger.debug("Merging conquer data with world data");
            try {
                for (ManageableType t : getAllElements()) {
                    Conquer c = (Conquer) t;
                    Village v = c.getVillage();
                    Tribe loser = c.getLoser();
                    Tribe winner = c.getWinner();
                    if (v.getTribeID() != winner.getId()) {
                        //conquer not yet in world data
                        if (loser != null && loser.removeVillage(v)) {
                            Ally loserAlly = loser.getAlly();
                            if (loserAlly != null) {
                                loserAlly.setVillages(loserAlly.getVillages() - 1);
                            }
                        }
                        if (winner != null && !winner.ownsVillage(v)) {
                            winner.addVillage(v, true);
                            v.setTribe(winner);
                            v.setTribeID(winner.getId());
                            Ally winnerAlly = winner.getAlly();
                            if (winnerAlly != null) {
                                winnerAlly.setVillages(winnerAlly.getVillages() - 1);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //setting last update to 0 to avoid errors
                lastUpdate = 0;
            }
            updateAcceptance();
            try {
                MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
            } catch (Exception e) {
            }
        } else {
            lastUpdate = 0;
            if (logger.isInfoEnabled()) {
                logger.info("Conquers file not found under '" + pFile + "'");
            }
        }
        revalidate();
    }

    @Override
    public void saveElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing conquers to '" + pFile + "'");
        }

        try {

            StringBuilder b = new StringBuilder();
            b.append("<conquers>\n");
            b.append("<lastUpdate>").append(getLastUpdate()).append("</lastUpdate>\n");
            for (ManageableType t : getAllElements()) {
                Conquer c = (Conquer) t;
                if (c != null) {
                    String xml = c.toXml();
                    if (xml != null) {
                        b.append(xml).append("\n");
                    }
                }
            }
            b.append("</conquers>");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("Conquers successfully saved");
        } catch (Throwable t) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save conquers", t);
            }
            //try to delete errornous file
            new File(pFile).delete();
        }
    }

    private void updateAcceptance() {
        invalidate();
        logger.debug("Filtering conquers");
        double risePerHour = ServerSettings.getSingleton().getSpeed() * ServerSettings.getSingleton().getRiseSpeed();
        logger.debug(" - using " + risePerHour + " as acceptance increment value");
        List<Conquer> toRemove = new LinkedList<Conquer>();
        for (ManageableType t : getAllElements().toArray(new ManageableType[]{})) {
            Conquer c = (Conquer) t;
            Village v = c.getVillage();
            if (v == null) {
                toRemove.add(c);
            } else {
                long time = c.getTimestamp();
                //get diff in seconds
                long diff = System.currentTimeMillis() / 1000 - time;
                //compare diff with estimated time for reaching 100% acceptance
                if (diff > (75 / risePerHour) * 60 * 60) {
                    //acceptance has grown at least to 100, mark conquer for removal
                    toRemove.add(c);
                }
            }
        }
        logger.debug("Removing " + toRemove.size() + " conquers due to 100% acceptance");
        for (Conquer remove : toRemove) {
            removeElement(remove);
        }
        revalidate();
        fireConquersChangedEvents();
    }

    protected void updateConquers() {
        logger.debug("Updating conquers from server");
        BufferedReader r = null;
        boolean error = false;
        try {
            if (lastUpdate == -1) {
                //not yet loaded
                return;
            }
            String baseUrl = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
            if (baseUrl == null) {//devel mode
                return;
            }
            if (System.currentTimeMillis() - lastUpdate > 1000 * 60 * 60 * 24) {
                //time larger than 24 hours, take 23 hours in past
                logger.debug("Last update more than 24h ago. Setting last update to NOW - 23h");
                lastUpdate = System.currentTimeMillis() - 1000 * 60 * 60 * 23;
            }

            URL u = new URL(baseUrl + "/interface.php?func=get_conquer&since=" + (int) Math.rint(lastUpdate / 1000));
            logger.debug("Querying " + u.toString());
            URLConnection uc = u.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
            r = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.indexOf("ONLY_ONE_DAY_AGO") != -1) {
                    logger.warn("Update still more than 24h ago. Diff to server clock > 1h ?");
                } else {
                    String[] data = line.split(",");
                    //$village_id, $unix_timestamp, $new_owner, $old_owner
                    int villageID = Integer.parseInt(data[0]);
                    int timestamp = Integer.parseInt(data[1]);
                    int newOwner = Integer.parseInt(data[2]);
                    int oldOwner = Integer.parseInt(data[3]);
                    boolean exists = false;
                    for (ManageableType t : getAllElements()) {//check if conquer exists
                        Conquer c = (Conquer) t;
                        if (c != null && c.getVillage() != null && c.getVillage().getId() == villageID) {
                            //already exists
                            c.setWinner(DataHolder.getSingleton().getTribes().get(newOwner));
                            c.setLoser(DataHolder.getSingleton().getTribes().get(oldOwner));
                            c.setTimestamp(timestamp);
                            exists = true;
                            break;
                        }
                    }

                    //continue with new conquers
                    if (!exists) {
                        Tribe loser = DataHolder.getSingleton().getTribes().get(oldOwner);
                        Tribe winner = DataHolder.getSingleton().getTribes().get(newOwner);
                        Village v = DataHolder.getSingleton().getVillagesById().get(villageID);

                        Conquer c = new Conquer();
                        c.setVillage(DataHolder.getSingleton().getVillagesById().get(villageID));
                        c.setTimestamp(timestamp);
                        c.setWinner(DataHolder.getSingleton().getTribes().get(newOwner));
                        c.setLoser(DataHolder.getSingleton().getTribes().get(oldOwner));
                        addConquer(c);

                        try {
                            //remove troop information for conquered village
                            VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                            if (holder != null) {
                                //check if troop holder state lays is before conquer
                                if (holder.getState().getTime() < timestamp) {
                                    //clear troops information for this village due to troop informations are outdated
                                    holder.clear();
                                    holder.setState(new Date(timestamp));
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        try {
                            //removing church
                            ChurchManager.getSingleton().removeChurch(v);
                        } catch (Exception ignored) {
                        }

                        if (winner != null && v != null && v.getTribeID() != winner.getId()) {
                            //conquer not yet in world data
                            if (loser != null && loser.removeVillage(v)) {
                                Ally loserAlly = loser.getAlly();
                                if (loserAlly != null) {
                                    loserAlly.setVillages(loserAlly.getVillages() - 1);
                                }
                            }
                            if (winner != null && !winner.ownsVillage(v)) {
                                winner.addVillage(v, true);
                                v.setTribe(winner);
                                v.setTribeID(winner.getId());
                                Ally winnerAlly = winner.getAlly();
                                if (winnerAlly != null) {
                                    winnerAlly.setVillages(winnerAlly.getVillages() - 1);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An error occured while updating conquers", e);
            error = true;
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (Exception inner) {
            }
        }
        if (!error) {
            lastUpdate = System.currentTimeMillis() + 1000;
            logger.debug("Setting lastUpdate to NOW (" + lastUpdate + ")");
        }
        updateAcceptance();
    }

    public Conquer getConquer(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        for (ManageableType t : getAllElements().toArray(new ManageableType[getElementCount()])) {
            Conquer c = (Conquer) t;
            if (c != null && pVillage != null && c.getVillage() != null && c.getVillage().getId() == pVillage.getId()) {
                return c;
            }
        }
        return null;
    }

    /**
     * @return the lastUpdate
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int[] getConquersStats() {
        int grey = 0;
        int friendly = 0;
        for (ManageableType t : getAllElements()) {
            Conquer c = (Conquer) t;
            if (c.getLoser() == null || c.getLoser().equals(Barbarians.getSingleton())) {
                grey++;
            } else {
                Tribe loser = c.getLoser();
                Tribe winner = c.getWinner();
                if (loser != null && winner != null) {
                    if (loser.getAllyID() == winner.getAllyID()) {
                        if (loser.getAllyID() != 0 && winner.getAllyID() != 0) {
                            friendly++;
                        }
                    } else {
                        Ally loserAlly = loser.getAlly();
                        Ally winnerAlly = winner.getAlly();
                        if (loserAlly != null && winnerAlly != null) {
                            String lAllyName = loserAlly.getName().toLowerCase();
                            String wAllyName = winnerAlly.getName().toLowerCase();
                            if (lAllyName.indexOf(wAllyName) > -1 || wAllyName.indexOf(lAllyName) > -1) {
                                friendly++;
                            }
                        }
                    }
                }
            }

        }
        return new int[]{grey, friendly};
    }

    /**
     * Notify all MarkerManagerListeners that the marker data has changed
     */
    private void fireConquersChangedEvents() {
        revalidate(true);
        try {
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
        } catch (Exception e) {
            //failed to initialize redraw because renderer is still null
        }
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        return "";
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        return true;
    }
}

class ConquerUpdateThread extends Thread {

    private final long FIVE_MINUTES = 1000 * 60 * 5;

    public ConquerUpdateThread() {
        setName("ConquerUpdateThread");
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                ConquerManager.getSingleton().updateConquers();
                try {
                    Thread.sleep(FIVE_MINUTES);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
    }
}
