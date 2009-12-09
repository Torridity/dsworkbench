/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.stat;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.TribeStatsElement;
import de.tor.tribes.util.GlobalOptions;
import java.io.File;
import java.io.FileFilter;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class StatManager {

    private static Logger logger = Logger.getLogger("StatManager");
    private static StatManager SINGLETON = null;
    private boolean INITIALIZED = false;
    private Hashtable<Integer, Hashtable<Integer, TribeStatsElement>> data = null;

    public static synchronized StatManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new StatManager();
        }
        return SINGLETON;
    }

    public void setup() {
        INITIALIZED = false;
        String dataPath = "./servers/" + GlobalOptions.getSelectedServer() + "/stats/";
        if (!new File(dataPath).exists()) {
            if (!new File(dataPath).mkdir()) {
                logger.error("Failed to create stats directory");
            }
        }
        data = new Hashtable<Integer, Hashtable<Integer, TribeStatsElement>>();

        File[] allyDirs = new File(dataPath).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        for (File allyDir : allyDirs) {
            try {
                Integer allyId = Integer.parseInt(allyDir.getName());
                Hashtable<Integer, TribeStatsElement> tribeStats = null;
                if (allyId == 0 || DataHolder.getSingleton().getAllies().get(allyId) != null) {
                    //directory for non ally tribes or valid ally dir found
                    tribeStats = readTribeStats(allyDir);
                    if (tribeStats != null) {
                        data.put(allyId, tribeStats);
                    }
                } else {
                    logger.info("No ally with ID '" + allyId + "' was found. Removing stat dir.");
                    deleteDirectory(allyDir);
                }
            } catch (Exception e) {
                //ally id invalid!? ignore!
            }
        }

        INITIALIZED = true;
    }

    private Hashtable<Integer, TribeStatsElement> readTribeStats(File pStatDir) {
        Hashtable<Integer, TribeStatsElement> tribeStats = new Hashtable<Integer, TribeStatsElement>();
        File[] tribeStatDirs = pStatDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        for (File tribeStatFile : tribeStatDirs) {
            try {
                Integer tribeId = Integer.parseInt(tribeStatFile.getName());
                if (DataHolder.getSingleton().getTribes().get(tribeId) == null) {
                    logger.info("No tribe with ID '" + tribeId + "' was found. Removing stat file.");
                    tribeStatFile.delete();
                }else{
                    //read tribe stats
                    TribeStatsElement elem = TribeStatsElement.loadFromFile(tribeStatFile);
                    if(elem != null){
                        tribeStats.put(tribeId, elem);
                    }
                }
            } catch (Exception e) {
            }
        }

        return tribeStats;
    }

    public boolean deleteDirectory(File directory) {
        File fileToDelete = directory;
        boolean result = false;
        if (fileToDelete.exists()) {
            File directoryFiles[] = fileToDelete.listFiles();
            for (int i = 0; i < directoryFiles.length; i++) {
                if (directoryFiles[i].isFile()) {
                    directoryFiles[i].delete();
                } else {
                    deleteDirectory(directoryFiles[i]);
                }
            }
            fileToDelete.delete();
            result = true;
        }

        return result;
    }
}

