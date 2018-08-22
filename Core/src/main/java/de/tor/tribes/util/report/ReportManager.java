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
package de.tor.tribes.util.report;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.SystrayHelper;
import de.tor.tribes.util.farm.FarmManager;
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.xml.JDomUtils;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class ReportManager extends GenericManager<FightReport> {

  private static Logger logger = LogManager.getLogger("ReportManager");
  private static ReportManager SINGLETON = null;
  public final static String FARM_SET = "Farmberichte";
  private List<ReportRule> rules = new LinkedList<>();
  private final ReportRule farmFilter = new ReportRule(ReportRule.RuleType.FARM, null, FARM_SET);

  public static synchronized ReportManager getSingleton() {
    if (SINGLETON == null) {
      SINGLETON = new ReportManager();
    }
    return SINGLETON;
  }

  ReportManager() {
    super(true);
    addGroup(FARM_SET);
  }

  @Override
  public void initialize() {
    super.initialize();
    addGroup(FARM_SET);
  }

  @Override
  public String[] getGroups() {
    String[] groups = super.getGroups();
    Arrays.sort(groups, new Comparator<String>() {

      @Override
      public int compare(String o1, String o2) {
        if (o1.equals(DEFAULT_GROUP) || o1.equals(FARM_SET)) {
          return -1;
        } else if (o2.equals(DEFAULT_GROUP) || o2.equals(FARM_SET)) {
          return 1;
        } else {
          return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
        }
      }
    });
    return groups;
  }

  public List<ReportRule> getRules() {
    return ListUtils.unmodifiableList(rules);
  }

  public void addRule(ReportRule pRule) {
    rules.add(pRule);
  }

  public void removeRule(ReportRule pRule) {
    logger.trace("Deleting Rule {} contains: {}", pRule.getStringRepresentation(), rules.contains(pRule));
    rules.remove(pRule);
  }

  @Override
  public void addManagedElement(final FightReport pElement) {
    boolean filtered = false;
    
    //update information of the Villages
    KnownVillageManager.getSingleton().updateInformation(pElement);
    
    if (farmFilter.isValid(pElement)) {
      logger.debug("Farm filter was activated for village " + pElement.getTargetVillage());
      FarmManager.getSingleton().updateFarmInfoFromReport(pElement);
      addManagedElement(FARM_SET, pElement, false);
    } else {
      for (ReportRule entry : getRules()) {
        if (entry.isValid(pElement)) {
          super.addManagedElement(entry.getTargetSet(), pElement);
          filtered = true;
          break;
        }
      }

      if (!filtered) {
        super.addManagedElement(pElement);
      }
      SystrayHelper.showInfoMessage("Bericht erfolgreich eingelesen");
    }
  }

  @Override
  public void addManagedElement(String pGroup, final FightReport pElement) {
    addManagedElement(pGroup, pElement, false);
  }

  public void addManagedElement(String pGroup, final FightReport pElement, boolean pFiltered) {
    boolean filtered = false;
    if (pFiltered) {
      if (farmFilter.isValid(pElement)) {
        logger.debug("Farm filter was activated for village " + pElement.getTargetVillage());
        FarmManager.getSingleton().updateFarmInfoFromReport(pElement);
        addManagedElement(FARM_SET, pElement, false);
      } else {
        for (ReportRule entry : getRules()) {
          if (entry.isValid(pElement)) {
            addManagedElement(entry.getTargetSet(), pElement);
            filtered = true;
            break;
          }
        }
      }
      if (!filtered) {
        super.addManagedElement(pGroup, pElement);
      }
      SystrayHelper.showInfoMessage("Bericht erfolgreich eingelesen");
    } else {//add element without filtering
      super.addManagedElement(pGroup, pElement);
    }
  }

  public int filterNow(String pGroup) {
    invalidate();
    try {
      HashMap<FightReport, String> newGroups = new HashMap<>();
      for (ManageableType t : getAllElements(pGroup)) {
        FightReport report = (FightReport) t;
        for (ReportRule entry : getRules()) {
          if (entry.isValid(report)) {
            if (!entry.getTargetSet().equals(pGroup)) {
              //only move report, if the filter points to a new group...
              //...otherwise, report stays in this group as the current filter is the first fits
              newGroups.put(report, entry.getTargetSet());
            }
            break;
          }
        }
      }

      Set<Entry<FightReport, String>> entries = newGroups.entrySet();
      for (Entry<FightReport, String> entry : entries) {
        //remove report from this group
        removeElement(pGroup, entry.getKey());
        //add report to new group and continue filtering
        addManagedElement(entry.getValue(), entry.getKey(), true);
      }
      return entries.size();
    } finally {
      revalidate(true);
    }
  }

  @Override
    public int importData(Element pElm, String pExtension) {
    if (pElm == null) {
        logger.error("Element argument is 'null'");
        return -1;
    }
    int result = 0;
    invalidate();

    logger.debug("Loading reports");
    try {
      for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "reportData/reportSet")) {
        String setKey = e.getAttributeValue("name");
        setKey = URLDecoder.decode(setKey, "UTF-8");
        if (pExtension != null) {
          setKey += "_" + pExtension;
        }
        addGroup(setKey);
        if (logger.isDebugEnabled()) {
          logger.debug("Loading report set ''{}", setKey);
        }

        for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "reports/report")) {
          FightReport r = new FightReport();
          r.loadFromXml(e1);
          addManagedElement(setKey, r);
          result++;
        }
      }
      logger.debug("Reports successfully loaded");
      
      for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "reportData/rule")) {
        ReportRule r = new ReportRule(e);
        rules.add(r);
      }
      logger.debug("Report Rules successfully loaded");
    } catch (Exception e) {
      result = result * (-1) - 1;
      logger.error("Failed to load reports", e);
    } finally {
      revalidate(true);
    }
    return result;
  }

  @Override
  public Element getExportData(final List<String> pGroupsToExport) {
    Element reportData = new Element("reportData");
    if (pGroupsToExport == null || pGroupsToExport.isEmpty()) {
        return reportData;
    }
    logger.debug("Generating report data");
    
    for (String set : pGroupsToExport) {
      Element reportSet = new Element("reportSet");
      reportSet.setAttribute("name", set);
      Element reports = new Element("reports");
      for (ManageableType t : getAllElements(set)) {
        reports.addContent(t.toXml("report"));
      }
      reportSet.addContent(reports);
      reportData.addContent(reportSet);
    }
    
    for(ReportRule r: rules) {
      reportData.addContent(r.toXml("rule"));
    }
    
    logger.debug("Data generated successfully");
    return reportData;
  }

  public boolean createReportSet(String pName) {
    return addGroup(pName);
  }

  /**
   * Return the most current report for source pVillage
   *
   * @param pVillage
   * @return
   */
  public FightReport findLastReportForSource(Village pVillage) {
    FightReport current = null;
    for (ManageableType element : getAllElementsFromAllGroups()) {
      FightReport report = (FightReport) element;
      if (report.getSourceVillage() != null && report.getSourceVillage().equals(pVillage)) {
        if (current == null || report.getTimestamp() > current.getTimestamp()) {
          current = report;
        }
      }
    }
    return current;
  }

  /**
   * Return the most current report for target pVillage
   *
   * @param pVillage
   * @return
   */
  public FightReport findLastReportForTarget(Village pVillage) {
    FightReport current = null;
    for (ManageableType element : getAllElementsFromAllGroups()) {
      FightReport report = (FightReport) element;
      if (report.getTargetVillage() != null && report.getTargetVillage().equals(pVillage)) {
        if (current == null || report.getTimestamp() > current.getTimestamp()) {
          current = report;
        }
      }
    }
    return current;
  }

  /**
   * Return all reports for target pVillage
   *
   * @param pVillage
   * @return
   */
  public List<FightReport> findAllReportsForTarget(Village pVillage) {
    List<FightReport> all = new LinkedList<>();
    for (ManageableType element : getAllElementsFromAllGroups()) {
      FightReport report = (FightReport) element;
      if (report.getTargetVillage() != null && report.getTargetVillage().equals(pVillage)) {
        all.add(report);
      }
    }
    return all;
  }

  /**
   * Return the most current report for pVillage
   *
   * @param pVillage
   * @return
   */
  public FightReport findLastReportForVillage(Village pVillage) {
    FightReport current = null;
    for (ManageableType element : getAllElementsFromAllGroups()) {
      FightReport report = (FightReport) element;
      if ((report.getTargetVillage() != null && report.getTargetVillage().equals(pVillage))
              || (report.getSourceVillage() != null && report.getSourceVillage().equals(pVillage))) {
        if (current == null || report.getTimestamp() > current.getTimestamp()) {
          current = report;
        }
      }
    }
    return current;
  }
}
