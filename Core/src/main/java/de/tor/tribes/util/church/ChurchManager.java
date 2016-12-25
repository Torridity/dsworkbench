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
package de.tor.tribes.util.church;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.Church;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Charon
 */
public class ChurchManager extends GenericManager<Church> {
    
    private static Logger logger = Logger.getLogger("ChurchManager");
    private static ChurchManager SINGLETON = null;
    
    public static synchronized ChurchManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ChurchManager();
        }
        return SINGLETON;
    }
    
    ChurchManager() {
        super(false);
    }
    
    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File churchFile = new File(pFile);
        if (churchFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading churches from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(churchFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//churches/church")) {
                    try {
                        Church c = new Church();
                        c.loadFromXml(e);
                        addManagedElement(c);
                    } catch (Exception inner) {
                        //ignored, marker invalid
                    }
                }
                logger.debug("Churches successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load churches", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Churches file not found under '" + pFile + "'");
            }
        }
        revalidate();
    }
    
    @Override
    public boolean importData(File pFile, String pExtension) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }
        boolean result = false;
        invalidate();
        logger.debug("Importing churches");
        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//churches/church")) {
                try {
                    Church c = new Church();
                    c.loadFromXml(e);
                    addManagedElement(c);
                } catch (Exception inner) {
                    //ignored, marker invalid
                }
            }
            logger.debug("Churches imported successfully");
            result = true;
        } catch (Exception e) {
            logger.error("Failed to import churches", e);
            MinimapPanel.getSingleton().redraw();
            result = false;
        }
        revalidate(true);
        return result;
    }
    
    @Override
    public String getExportData(List<String> pGroupsToExport) {
        logger.debug("Generating churches export data");
        
        String result = "<churches>\n";
        ManageableType[] elements = getAllElements().toArray(new ManageableType[getAllElements().size()]);
        
        for (ManageableType t : elements) {
            Church c = (Church) t;
            result += c.toXml() + "\n";
        }
        result += "</churches>\n";
        logger.debug("Export data generated successfully");
        return result;
    }

    /**
     * Load markers from database (not implemented yet)
     */
    public void loadChurchesFromDatabase(String pUrl) {
        logger.info("Not implemented yet");
    }
    
    @Override
    public void saveElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Writing churches to '" + pFile + "'");
        }
        try {
            StringBuilder b = new StringBuilder();
            
            b.append("<churches>\n");
            for (ManageableType t : getAllElements()) {
                Church c = (Church) t;
                b.append(c.toXml()).append("\n");
            }
            b.append("</churches>");
            //write data to file
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("Churches successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save churches", e);
            }
        }
    }

    /**
     * Save markers to database (not implemented yet)
     */
    public void saveChurchesToDatabase() {
        logger.info("Not implemented yet");
    }
    
    public Church getChurch(Village v) {
        if (v == null) {
            return null;
        }
        for (ManageableType t : getAllElements()) {
            Church c = (Church) t;
            if (c.getVillage().equals(v)) {
                return c;
            }
        }
        return null;
    }
    
    public List<Village> getChurchVillages() {
        List<Village> villages = new LinkedList<>();
        
        for (ManageableType t : getAllElements()) {
            Church c = (Church) t;
            villages.add(c.getVillage());
        }
        return villages;
    }
    
    public void addChurch(Village pVillage, int pRange) {
        if (pVillage != null) {
            Church c = getChurch(pVillage);
            if (c == null) {
                c = new Church();
                c.setVillage(pVillage);
                c.setRange(pRange);
                addManagedElement(c);
            } else {
                c.setRange(pRange);
            }
        }
    }
    
    public void removeChurch(Village pVillage) {
        if (pVillage != null) {
            Church toRemove = null;
            for (ManageableType t : getAllElements()) {
                Church c = (Church) t;
                if (c.getVillage().equals(pVillage)) {
                    toRemove = c;
                    break;
                }
            }
            
            if (toRemove != null) {
                removeElement(toRemove);
            }
        }
    }
    
    public void removeChurches(Village[] pVillages) {
        if (pVillages != null) {
            invalidate();
            for (Village v : pVillages) {
                if (v != null) {
                    //village is valid
                    List<Church> toRemove = new ArrayList<>();
                    for (ManageableType t : getAllElements()) {
                        Church c = (Church) t;
                        //iterate through all churches
                        if (c.getVillage().equals(v)) {
                            //remove current church
                            toRemove.add(c);
                        }
                    }
                    
                    for (Church c : toRemove) {
                        removeElement(c);
                    }
                    
                }
            }
            revalidate(true);
        }
    }
}
