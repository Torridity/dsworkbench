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
package de.tor.tribes.util.farm;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.*;
import de.tor.tribes.util.*;
import de.tor.tribes.util.report.ReportManager;
import de.tor.tribes.util.xml.JDomUtils;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class FarmManager extends GenericManager<FarmInformation> {

  private static Logger logger = LogManager.getLogger("FarmManager");
  private static FarmManager SINGLETON = null;
  private Hashtable<Village, FarmInformation> infoMap = null;
  private Village lastUpdatedFarm = null;

  public static synchronized FarmManager getSingleton() {
    if (SINGLETON == null) {
      SINGLETON = new FarmManager();
    }
    return SINGLETON;
  }

  FarmManager() {
    super(false);
    infoMap = new Hashtable<>();
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
      logger.debug("Updating farm information for farm " + info.getVillage());
      lastUpdatedFarm = info.getVillage();
      info.updateFromReport(pReport);
      SystrayHelper.showInfoMessage("Farminformationen für " + info.getVillage() + " aktualisiert");
    }
  }

  public int findFarmsInClipboard() {
    int addCount = 0;
    try {
      String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
      List<Village> villages = PluginManager.getSingleton().executeVillageParser(data);
      List<Village> handled = new LinkedList<>();

      for (Village farm : villages) {
        if (farm != null && !handled.contains(farm) && farm.getTribe().equals(Barbarians.getSingleton())) {
          FarmInformation info = addFarm(farm);
          if (info.isJustCreated()) {
            FightReport r = ReportManager.getSingleton().findLastReportForSource(farm);
            if (r != null) {
              info.updateFromReport(r);
            } else {
              info.setInitialResources();
            }
            addCount++;
            handled.add(farm);
          }
        }
      }
    } catch (HeadlessException | IOException | UnsupportedFlavorException he) {
      logger.error("Failed to find farms in clipboard", he);
    }
      return addCount;
  }

  public int findFarmsInReports(boolean pAllowAloneTribes, boolean pAllowTribesInAllies, boolean pAllowTribesInOwnAlly) {
    return findFarmsInReports(null, pAllowAloneTribes, pAllowTribesInAllies, pAllowTribesInOwnAlly);
  }

  public int findFarmsInReports(String pReportSet, boolean pAllowAloneTribes, boolean pAllowTribesInAllies, boolean pAllowTribesInOwnAlly) {
    int addCount = 0;
    List<Village> handled = new LinkedList<>();
    Tribe yourTribe = GlobalOptions.getSelectedProfile().getTribe();
    invalidate();
    //get all groups but the farm group itself
    List<String> searchInGroups = new LinkedList<>();
    if (pReportSet == null) {
      for (String group : ReportManager.getSingleton().getGroups()) {
        if (!group.equals(ReportManager.FARM_SET)) {
          searchInGroups.add(group);
        }
        logger.debug(searchInGroups.size() + "groups are used");
      }
    } else {
        searchInGroups.add(pReportSet);
        logger.debug(searchInGroups.size() + "groups are used");
    }
    logger.debug(ReportManager.getSingleton().getAllElements(searchInGroups).size() + " Elements are found");
    for (ManageableType t : ReportManager.getSingleton().getAllElements(searchInGroups)) {
    	
      FightReport report = (FightReport) t;
      Village target = report.getTargetVillage();
      if (report.isWon() && !(target.getTribe().getId() == yourTribe.getId())) {
        boolean allowed;
        if (report.getTargetVillage().getTribe().getAlly() != null && report.getTargetVillage().getTribe().getAlly().equals(yourTribe.getAlly())) {
        	logger.debug("Village counts as ally");
          //own ally
          allowed = pAllowTribesInOwnAlly;
        } else {//target is not in own ally
        	logger.debug("Not an ally");
          if (!report.getTargetVillage().getTribe().equals(Barbarians.getSingleton())) {
        	  logger.debug("Village tribe is not barbarian");
            //village has owner
            if ((report.getTargetVillage().getTribe().getAlly() == null || report.getTargetVillage().getTribe().getAlly().equals(NoAlly.getSingleton()))) {
              //owner has no ally
            	logger.debug("Village is not Barbarian and is not an ally ally");
              allowed = pAllowAloneTribes;
            } else {
            	logger.debug("Village is not Barbarian and is an ally ally");
              //tribe is in ally
              allowed = pAllowTribesInAllies;
            }
          } else {
            //Barbarians
            allowed = true;
            logger.debug("Report is about Barbarian");
          }
        }
        
        logger.debug("Start adding the farms");
        if (allowed) {
          if (!handled.contains(target)) {//add farm
            FarmInformation info = addFarm(target);
            info.updateFromReport(report);
            handled.add(target);
            if (info.isJustCreated()) {
              addCount++;
            }
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
    logger.debug(addCount + " farms are found");
    revalidate(true);
    return addCount;
  }

  /**
   * Find barbarians in radius around all own villages
   */
  public int findFarmsFromBarbarians(int pRadius) {
    int addCount = 0;
    invalidate();
    for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
      Ellipse2D.Double e = new Ellipse2D.Double((int) v.getX() - pRadius, (int) v.getY() - pRadius, 2 * pRadius, 2 * pRadius);
      
      Rectangle mapDim = ServerSettings.getSingleton().getMapDimension();
      for (int x = (int) v.getX() - pRadius; x < (int) v.getX() + pRadius; x++) {
        for (int y = (int) v.getY() - pRadius; y < (int) v.getY() + pRadius; y++) {
          if (x >= mapDim.getMinX() && x <= mapDim.getMaxX()
                  && y >= mapDim.getMinY() && y <= mapDim.getMaxY()) {
            if (e.contains(new Point2D.Double(x, y))) {
              Village farm = DataHolder.getSingleton().getVillages()[x][y];
              if (farm != null && farm.getTribe().equals(Barbarians.getSingleton())) {
                FarmInformation info = addFarm(farm);
                if (info.isJustCreated()) {
                  FightReport r = ReportManager.getSingleton().findLastReportForSource(farm);
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
      }

    }
    revalidate(true);
    return addCount;
  }

  public int findFarmsFromBarbarians(Point pCenter, int pRadius) {
    int addCount = 0;
    invalidate();
    Ellipse2D.Double e = new Ellipse2D.Double(pCenter.x - pRadius, pCenter.y - pRadius, 2 * pRadius, 2 * pRadius);
      
    Rectangle mapDim = ServerSettings.getSingleton().getMapDimension();
    for (int i = pCenter.x - pRadius; i < pCenter.x + pRadius; i++) {
      for (int j = pCenter.y - pRadius; j < pCenter.y + pRadius; j++) {
        if (i >= mapDim.getMinX() && i <= mapDim.getMaxX()
            && j >= mapDim.getMinY() && j <= mapDim.getMaxY()) {
          if (e.contains(new Point2D.Double(i, j))) {
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
    }
    revalidate(true);
    return addCount;
  }

  public void revalidateFarms() {
    for (ManageableType t : getAllElements()) {
      ((FarmInformation) t).revalidate();
    }
  }

  public Village getLastUpdatedFarm() {
    return lastUpdatedFarm;
  }

  @Override
  public void loadElements(String pFile) {
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return;
    }
    invalidate();
    initialize();
    File farmFile = new File(pFile);
    infoMap.clear();
    if (farmFile.exists()) {
      logger.debug("Reading farm information from file " + pFile);
      try {
        Document d = JDomUtils.getDocument(farmFile);
        for (Element e : (List<Element>) JDomUtils.getNodes(d, "farmInfos/farmInfo")) {
          FarmInformation element = new FarmInformation(e);
          if (element.getVillage() != null) {
            //just add valid information
            element.revalidate();
            addManagedElement(element);
            infoMap.put(element.getVillage(), element);
          }
        }
        logger.debug("Farm information successfully read");
      } catch (Exception e) {
        logger.error("Failed to read farm information", e);
      }
    } else {
        logger.info("No FarmInformation found under '" + pFile + "'");
    }
    revalidate(true);
  }

  @Override
  public void saveElements(String pFile) {
    logger.debug("Writing farm information to file " + pFile);
    try (FileWriter w = new FileWriter(pFile)) {
      w.write("<data><farmInfos>\n");
      for (ManageableType element : getAllElements()) {
        w.write(element.toXml());
      }
      w.write("</farmInfos></data>\n");
      w.flush();
      w.close();
    } catch (Exception e) {
      logger.error("Failed to write farm information", e);
    }
  }

  @Override
  public String getExportData(List<String> pGroupsToExport) {
      StringBuilder expData = new StringBuilder();
      expData.append("<farmInfos>\n");
      for (ManageableType element : getAllElements()) {
        expData.append(element.toXml());
      }
      expData.append("</farmInfos>\n");
    return expData.toString();
  }

  @Override
  public boolean importData(File pFile, String pExtension) {
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return false;
    }
    invalidate();
    if (pFile.exists()) {
      logger.debug("Reading farm information from file " + pFile);
      try {
        Document d = JDomUtils.getDocument(pFile);
        for (Element e : (List<Element>) JDomUtils.getNodes(d, "farmInfos/farmInfo")) {
          FarmInformation element = new FarmInformation(e);
          if (element.getVillage() != null) {
            //just add valid information
            element.revalidate();
            addManagedElement(element);
            infoMap.put(element.getVillage(), element);
          }
        }
        logger.debug("Farm information successfully read");
      } catch (Exception e) {
        logger.error("Failed to read farm information", e);
        revalidate(true);
        return false;
      }
    } else {
      logger.info("No FarmInformation found under '" + pFile + "'");
      revalidate(true);
      return false;
    }
    revalidate(true);
    return true;
  }
}
