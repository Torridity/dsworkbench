/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.farm;

import com.thoughtworks.xstream.XStream;
import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.TroopHelper;
import de.tor.tribes.util.report.ReportManager;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class FarmManager extends GenericManager<FarmInformation> {

    private static Logger logger = Logger.getLogger("FarmManager");
    private static FarmManager SINGLETON = null;
    private Hashtable<Village, FarmInformation> infoMap = null;

    public static synchronized FarmManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new FarmManager();
        }
        return SINGLETON;
    }

    FarmManager() {
        super(false);
        infoMap = new Hashtable<Village, FarmInformation>();
    }

    public void addBarbarian(Village pVillage, int pRadius) {
        for (int i = pVillage.getX() - pRadius; i < pVillage.getX() + pRadius; i++) {
            for (int j = pVillage.getY() - pRadius; j < pVillage.getY() + pRadius; j++) {
                if (i < ServerSettings.getSingleton().getMapDimension().width && j < ServerSettings.getSingleton().getMapDimension().height) {
                    Village farm = DataHolder.getSingleton().getVillages()[i][j];
                    if (farm != null && farm.getTribe().equals(Barbarians.getSingleton())) {
                        FarmInformation info = addFarm(farm);
                        info.updateFromReport(ReportManager.getSingleton().findLastReportForTarget(pVillage));
                    }
                }
            }
        }
    }

    public void addFromReports() {
        List<Village> handled = new LinkedList<Village>();
        for (ManageableType t : ReportManager.getSingleton().getAllElements()) {
            FightReport r = (FightReport) t;
            if (!handled.contains(r.getTargetVillage())) {
                if (TroopHelper.isEmpty(r.getSurvivingDefenders())) {
                    FarmInformation info = addFarm(r.getTargetVillage());
                    info.updateFromReport(ReportManager.getSingleton().findLastReportForTarget(r.getTargetVillage()));
                    handled.add(r.getTargetVillage());
                }
            }
        }
    }

    public FarmInformation addFarm(Village pVillage) {
        FarmInformation info = null;
        if (!infoMap.containsKey(pVillage)) {
            info = new FarmInformation(pVillage);
            addManagedElement(info);
        } else {
            infoMap.get(pVillage);
        }
        return info;
    }

    public void removeFarm(Village pVillage) {
        FarmInformation toRemove = infoMap.remove(pVillage);
        removeElement(toRemove);
    }

    public FarmInformation getFarmInformation(Village pVillage) {
        return infoMap.get(pVillage);
    }

    public void updateFarmInfoFromReport(FightReport pReport) {
        FarmInformation info = getFarmInformation(pReport.getTargetVillage());
        if (info != null) {
            info.updateFromReport(pReport);
        }
    }

    @Override
    public void loadElements(String pFile) {
        XStream x = new XStream();
        x.alias("farmInfo", FarmInformation.class);
        FileReader r = null;
        logger.debug("Reading farm information from file " + pFile);
        try {
            r = new FileReader(pFile);
            List<ManageableType> el = (List<ManageableType>) x.fromXML(r);
            invalidate();
            for (ManageableType t : el) {
                FarmInformation info = (FarmInformation) t;
                info.revalidate();
                addManagedElement(info);
            }
            r.close();
            logger.debug("Farm information successfully read");
        } catch (IOException ioe) {
            logger.error("Failed to read farm information", ioe);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException ignored) {
            }
        }
        revalidate();
    }

    @Override
    public void saveElements(String pFile) {
        XStream x = new XStream();
        x.alias("farmInfo", FarmInformation.class);
        logger.debug("Writing farm information to file " + pFile);
        FileWriter w = null;
        try {
            w = new FileWriter(pFile);
            x.toXML(getAllElements(), w);
            w.flush();
        } catch (IOException ioe) {
            logger.error("Failed to write farm information", ioe);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        return null;
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        return false;
    }
}
