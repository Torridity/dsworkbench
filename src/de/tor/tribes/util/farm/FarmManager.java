/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.farm;

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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class FarmManager extends GenericManager<FarmInformation> {

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
    }

    @Override
    public void saveElements(String pFile) {
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
