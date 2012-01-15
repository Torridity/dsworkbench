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
import de.tor.tribes.types.ext.BarbarianAlly;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.TroopHelper;
import de.tor.tribes.util.report.ReportManager;
import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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

    public FarmInformation addFarm(Village pVillage) {
        FarmInformation info = infoMap.get(pVillage);
        if (info == null) {
            info = new FarmInformation(pVillage);
            infoMap.put(pVillage, info);
            info.setJustCreated(true);
            addManagedElement(info);
        } else {
            info.setJustCreated(false);
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

    public int findFarmsInReports(int pRadius) {
        int addCount = 0;
        List<Village> handled = new LinkedList<Village>();
        Tribe yourTribe = GlobalOptions.getSelectedProfile().getTribe();
        Point center = DSCalculator.calculateCenterOfMass(Arrays.asList(yourTribe.getVillageList()));
        invalidate();
        for (ManageableType t : ReportManager.getSingleton().getAllElementsFromAllGroups()) {
            FightReport report = (FightReport) t;
            Village target = report.getTargetVillage();
            if (report.isWon()) {
                if (DSCalculator.calculateDistance(new Village(center.x, center.y), report.getTargetVillage()) <= pRadius) {//in radius
                    if (!report.getTargetVillage().getTribe().equals(t)
                            && (report.getTargetVillage().getTribe().getAlly() == null
                            || report.getTargetVillage().getTribe().getAlly().equals(BarbarianAlly.getSingleton())
                            || !report.getTargetVillage().getTribe().getAlly().equals(yourTribe.getAlly()))) {
                        if (!handled.contains(target)) {//add farm
                            FarmInformation info = addFarm(target);
                            info.updateFromReport(report);
                            handled.add(target);
                            addCount++;
                        } else {//update to newer report
                            FarmInformation info = getFarmInformation(target);
                            if (info != null && info.getLastReport() < report.getTimestamp()) {
                                info.updateFromReport(report);
                                if (info.getStatus().equals(FarmInformation.FARM_STATUS.CONQUERED)
                                        || info.getStatus().equals(FarmInformation.FARM_STATUS.TROOPS_FOUND)) {
                                    removeFarm(target);
                                    addCount--;
                                }
                            }
                        }
                    }
                }
            }
        }
        revalidate(true);
        return addCount;
    }

    public int findFarmsFromBarbarians(int pRadius) {
        int addCount = 0;
        Tribe yourTribe = GlobalOptions.getSelectedProfile().getTribe();
        Point center = DSCalculator.calculateCenterOfMass(Arrays.asList(yourTribe.getVillageList()));
        invalidate();
        for (int i = center.x - pRadius; i < center.x + pRadius; i++) {
            for (int j = center.y - pRadius; j < center.y + pRadius; j++) {
                if (i > 0 && i < ServerSettings.getSingleton().getMapDimension().width
                        && j > 0 && j < ServerSettings.getSingleton().getMapDimension().height) {
                    Village v = DataHolder.getSingleton().getVillages()[i][j];
                    if (v != null && v.getTribe().equals(Barbarians.getSingleton())) {
                        FarmInformation info = addFarm(v);
                        if (info.isJustCreated()) {
                            FightReport r = ReportManager.getSingleton().findLastReportForSource(v);
                            if (r != null) {
                                info.updateFromReport(r);
                            } else {
                                info.setInitialResources();
                            }
                            addCount++;
                        }
                    }
                }
            }
        }
        revalidate(true);
        return addCount;
    }

    public void revalidateFarms() {
        for (ManageableType t : getAllElements()) {
            ((FarmInformation) t).revalidate();
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
            initialize();
            for (ManageableType t : el) {
                FarmInformation info = (FarmInformation) t;
                info.revalidate();
                addManagedElement(info);
                infoMap.put(info.getVillage(), info);
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
