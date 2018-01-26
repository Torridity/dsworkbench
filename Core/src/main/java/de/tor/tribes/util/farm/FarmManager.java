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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.FarmInformation;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.*;
import de.tor.tribes.util.*;
import de.tor.tribes.util.report.ReportManager;
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
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class FarmManager extends GenericManager<FarmInformation> {

  private static Logger logger = Logger.getLogger("FarmManager");
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
      }
    } else {
      searchInGroups.add(pReportSet);
    }

    for (ManageableType t : ReportManager.getSingleton().getAllElements(searchInGroups)) {
      FightReport report = (FightReport) t;
      Village target = report.getTargetVillage();
      if (report.isWon() && !(target.getTribe().getId() == yourTribe.getId())) {
        boolean allowed;
        if (report.getTargetVillage().getTribe().getAlly() != null && report.getTargetVillage().getTribe().getAlly().equals(yourTribe.getAlly())) {
          //own ally
          allowed = pAllowTribesInOwnAlly;
        } else {//target is not in own ally
          if (!report.getTargetVillage().getTribe().equals(Barbarians.getSingleton())) {
            //village has owner
            if ((report.getTargetVillage().getTribe().getAlly() == null || report.getTargetVillage().getTribe().getAlly().equals(NoAlly.getSingleton()))) {
              //owner has no ally
              allowed = pAllowAloneTribes;
            } else {
              //tribe is in ally
              allowed = pAllowTribesInAllies;
            }
          } else {
            //Barbarians
            allowed = true;
          }
        }
        
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
    XStream x = new XStream();
    x.alias("farmInfo", FarmInformation.class);
    lastUpdatedFarm = null;
    FileReader r = null;
    logger.debug("Reading farm information from file " + pFile);
    initialize();
    infoMap.clear();
    try {
      r = new FileReader(pFile);
      List<ManageableType> el = (List<ManageableType>) x.fromXML(r);
      invalidate();
      for (ManageableType t : el) {
        FarmInformation info = (FarmInformation) t;
        if (info.getVillage() != null) {
          //just add valid information
          info.revalidate();
          addManagedElement(info);
          infoMap.put(info.getVillage(), info);
        }
      }
      r.close();
      logger.debug("Farm information successfully read");
    } catch (Exception e) {
      logger.error("Failed to read farm information", e);
    } finally {
      try {
        if (r != null) {
          r.close();
        }
      } catch (IOException ignored) {
      }
    }

    revalidate(true);
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
    XStream x = new XStream();
    x
            .alias("farmInfo", FarmInformation.class
            );
    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    try {
      x.toXML(getAllElements(), bout);
      bout.flush();
      bout.close();
    } catch (IOException ioe) {
      logger.error("Failed to write farm information", ioe);
    } finally {
      try {
        if (bout != null) {
          bout.close();
        }
      } catch (IOException ignored) {
      }
    }

    return bout.toString();
  }

  @Override
  public boolean importData(File pFile, String pExtension) {
    StringBuilder b = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(pFile));
      String line = "";
      boolean haveStart = false;
      String end = "</java.util.Collections_-UnmodifiableRandomAccessList>";
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("<java.util.Collections")) {
          haveStart = true;
        }

        if (haveStart) {
          if (line.startsWith(end)) {
            b.append(line.substring(0, end.length()));
          } else {
            b.append(line);
          }
        }
      }
    } catch (IOException e) {
      logger.error("Failed to farm data from file", e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ignored) {
        }
      }
    }
    XStream x = new XStream();
    x
            .alias("farmInfo", FarmInformation.class
            );
    lastUpdatedFarm = null;

    initialize();

    infoMap.clear();

    try {
      List<ManageableType> el = (List<ManageableType>) x.fromXML(b.toString());
      invalidate();
      for (ManageableType t : el) {
        FarmInformation info = (FarmInformation) t;
        info.revalidate();
        addManagedElement(info);
        infoMap.put(info.getVillage(), info);
      }
      logger.debug("Farm information successfully imported");
    } catch (ConversionException ce) {
      logger.error("Failed to deserialize farm information", ce);
      return false;
    } finally {
      revalidate(true);
    }

    return true;
  }
}
