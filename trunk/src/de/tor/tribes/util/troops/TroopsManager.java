/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.troops;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Charon
 */
public class TroopsManager {

    private static Logger logger = Logger.getLogger(TroopsManager.class);
    private static TroopsManager SINGLETON = null;
    private Hashtable<Village, List<Integer>> mTroopsTable = null;

    public static synchronized TroopsManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TroopsManager();
        }
        return SINGLETON;
    }

    TroopsManager() {
        mTroopsTable = new Hashtable<Village, List<Integer>>();
    }

    public void loadTroopsFromDatabase(String pUrl) {
        //not yet implemented
    }

    public void addTroopsForVillage(Village pVillage, List<Integer> pTroops) {
        mTroopsTable.put(pVillage, pTroops);
    }
    
    public List<Integer> getTroopsForVillage(Village pVillage){
        return mTroopsTable.get(pVillage);
    }

    public void loadTroopsFromFile(String pFile) {
        mTroopsTable.clear();
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
                    //get basic village without merged information
                    Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("id").getText()));
                    //get correct village
                    v = DataHolder.getSingleton().getVillages()[v.getX()][v.getY()];
                    List<Integer> troops = new LinkedList<Integer>();
                    for (Element t : (List<Element>) JaxenUtils.getNodes(e, "troops/troop")) {
                        troops.add(Integer.parseInt(t.getText()));
                    }
                    mTroopsTable.put(v, troops);
                }
                logger.debug("Troops loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load troops", e);
            }
        } else {
            logger.info("No troops found under '" + pFile + "'");
        }
    }

    public void saveTroopsToDatabase(String pUrl) {
        //not implemented yet
    }

    public void saveTroopsToFile(String pFile) {
        try {
            FileWriter w = new FileWriter(pFile);
            w.write("<villages>\n");
            Enumeration<Village> villages = mTroopsTable.keys();
            while (villages.hasMoreElements()) {
                //write village information
                Village v = villages.nextElement();
                w.write("<village>\n");
                w.write("<id>" + v.getId() + "</id>\n");
                w.write("<troops>\n");
                for (Integer i : mTroopsTable.get(v)) {
                    //write troop information
                    w.write("<troop>" + i + "</troops>\n");
                }
                //close troops for village
                w.write("</troops>\n");
                w.write("</village>\n");
            }
            //close all
            w.write("</villages>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store troops", e);
        }
    }

    public static void main(String[] args) throws Exception {
        String data = "<villages><village><id>666</id><troops><troop>500</troop><troop>800</troop></troops></village><village><id>777</id><troops><troop>111</troop><troop>7845</troop></troops></village></villages>";
        Document d = JaxenUtils.getDocument(data);
        for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//villages/village")) {
            //Village v = null;//DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(e.getChild("village").getText()));
            System.out.println(e.getChild("id").getText());
            List<Integer> troops = new LinkedList<Integer>();
            for (Element t : (List<Element>) JaxenUtils.getNodes(e, "troops/troop")) {
                //troops.add(Integer.parseInt(t.getText()));
                System.out.println(t.getText());
            }
        }
    }
}
