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
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.renderer.map.MapRenderer;
import de.tor.tribes.ui.views.DSWorkbenchSettingsDialog;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.xml.JDomUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * @author Charon
 */
public class ConquerManager extends GenericManager<Conquer> {
    private static Logger logger = LogManager.getLogger("ConquerManager");
    
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
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        invalidate();
        logger.info("Reading conquer");
        
        try {
            String lastup = JDomUtils.getNodeValue(pElm, "conquers/lastUpdate");
            this.lastUpdate = Long.parseLong(lastup);
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "conquers/conquer")) {
                try {
                    Conquer c = new Conquer();
                    c.loadFromXml(e);
                    addConquer(c);
                    result++;
                } catch (Exception inner) {
                    //ignored, conquer invalid
                }
            }
            logger.debug("Conquers successfully loaded");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load conquers", e);
        }

        if (lastUpdate == -1) {
            //set update correct on error
            this.lastUpdate = 0;
        }
        mergeWithWorldData();
        
        revalidate();
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element conquers = new Element("conquers");
        if (pGroupsToExport == null || pGroupsToExport.isEmpty()) {
            return conquers;
        }
        logger.debug("Generating conquers XML");
        
        try {
            conquers.addContent(new Element("lastUpdate").setText(Long.toString(lastUpdate)));
            
            for (ManageableType t : getAllElements()) {
                conquers.addContent(t.toXml("conquer"));
            }
            logger.debug("Conquers XML successfully generated");
        } catch (Exception e) {
            logger.debug("Failed to generate XML", e);
        }
        return conquers;
    }
    
    private void mergeWithWorldData() {
        //merge conquers and world data
        logger.debug("Merging conquer data with world data");
        try {
            for (ManageableType t : getAllElements()) {
                Conquer c = (Conquer) t;
                Village v = c.getVillage();
                Tribe loser = c.getLoser();
                Tribe winner = c.getWinner();
                if (winner != null && v.getTribeID() != winner.getId()) {
                    //conquer not yet in world data
                    if (loser != null && loser.removeVillage(v)) {
                        Ally loserAlly = loser.getAlly();
                        if (loserAlly != null) {
                            loserAlly.setVillages(loserAlly.getVillages() - 1);
                        }
                    }
                    
                    winner.addVillage(v, true);
                    v.setTribe(winner);
                    v.setTribeID(winner.getId());
                    Ally winnerAlly = winner.getAlly();
                    if (winnerAlly != null) {
                        winnerAlly.setVillages(winnerAlly.getVillages() - 1);
                    }
                }
            }
        } catch (Exception e) {
        }
        
        updateAcceptance();
        try {
            MapPanel.getSingleton().getMapRenderer().initiateRedraw(MapRenderer.TAG_MARKER_LAYER);
        } catch (Exception ignored) {
        }
    }

    private void updateAcceptance() {
        invalidate();
        logger.debug("Filtering conquers");
        double risePerHour = DSCalculator.calculateRiseSpeed();
        logger.debug(" - using " + risePerHour + " as acceptance increment value");
        List<Conquer> toRemove = new LinkedList<>();
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
            logger.debug("Querying {}", u);
            URLConnection uc = u.openConnection(DSWorkbenchSettingsDialog.getSingleton().getWebProxy());
            r = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("ONLY_ONE_DAY_AGO")) {
                    logger.warn("Update still more than 24h ago. Diff to server clock > 1h ?");
                    continue;
                }
                
                String[] data = line.split(",");
                //$village_id, $unix_timestamp, $new_owner, $old_owner
                int villageID = Integer.parseInt(data[0]);
                int timestamp = Integer.parseInt(data[1]);
                int newOwner = Integer.parseInt(data[2]);
                int oldOwner = Integer.parseInt(data[3]);
                Tribe loser = DataHolder.getSingleton().getTribes().get(oldOwner);
                Tribe winner = DataHolder.getSingleton().getTribes().get(newOwner);
                boolean exists = false;
                for (ManageableType t : getAllElements()) {//check if conquer exists
                    Conquer c = (Conquer) t;
                    if (c != null && c.getVillage() != null && c.getVillage().getId() == villageID
                            && c.getWinner() == winner && c.getLoser() == loser
                            && c.getTimestamp() == timestamp) {
                        exists = true;
                        break;
                    }
                }

                //continue with new conquers
                if (!exists) {
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
                        KnownVillageManager.getSingleton().removeChurch(v);
                    } catch (Exception ignored) {
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
            } catch (Exception ignored) {
            }
        }
        if (!error) {
            lastUpdate = System.currentTimeMillis() - 1000;
            logger.debug("Setting lastUpdate to NOW (" + lastUpdate + ")");
        }
        mergeWithWorldData();
    }

    public Conquer getConquer(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        for (ManageableType t : getAllElements()) {
            Conquer c = (Conquer) t;
            if (c != null && c.getVillage() != null && c.getVillage().getId() == pVillage.getId()) {
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
        int self = 0;
        
        for (ManageableType t : getAllElements()) {
            Conquer c = (Conquer) t;
            if (c.getLoser() == null || c.getLoser().equals(Barbarians.getSingleton())) {
                grey++;
            } else if(c.getWinner() != null && c.getLoser().equals(c.getWinner())) {
                self++;
            } else if(c.getWinner() != null && c.getLoser().getAllyID() == c.getWinner().getAllyID()
                    && c.getLoser().getAllyID() != 0) {
                friendly++;
            }

        }
        return new int[]{grey, friendly, self};
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
}

class ConquerUpdateThread extends Thread {
    private static Logger logger = LogManager.getLogger("ConquerUpdateThread");

    private static final long FIVE_MINUTES = 1000 * 60 * 5;

    public ConquerUpdateThread() {
        setName("ConquerUpdateThread");
        setPriority(MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConquerManager.getSingleton().updateConquers();
                Thread.sleep(FIVE_MINUTES);
            }
        } catch (Exception e) {
            logger.debug("Exception in Conquer thread shutting it down", e);
        }
    }
}
