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
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.SystrayHelper;
import de.tor.tribes.util.farm.FarmManager;
import de.tor.tribes.util.village.KnownVillageManager;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class ReportManager extends GenericManager<FightReport> {

  private static Logger logger = Logger.getLogger("ReportManager");
  private static ReportManager SINGLETON = null;
  public final static String FARM_SET = "Farmberichte";
  private List<RuleEntry> rules = new LinkedList<>();
  private final FarmReportFilter farmFilter = new FarmReportFilter();

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

  public RuleEntry[] getRuleEntries() {
    return rules.toArray(new RuleEntry[rules.size()]);
  }

  public void addRule(ReportRuleInterface pFilter, String pToSet) {
    rules.add(new RuleEntry(pFilter, pToSet));
  }

  public void removeRule(ReportRuleInterface pFilter) {
    for (RuleEntry rule : rules.toArray(new RuleEntry[rules.size()])) {
      if (rule.getRule().equals(pFilter)) {
        rules.remove(rule);
        break;
      }
    }
  }

  @Override
  public void addManagedElement(final FightReport pElement) {
    boolean filtered = false;
    
    //update information of the VIllages
    KnownVillageManager.getSingleton().updateInformation(pElement);
    
    if (farmFilter.isValid(pElement)) {
      logger.debug("Farm filter was activated for village " + pElement.getTargetVillage());
      FarmManager.getSingleton().updateFarmInfoFromReport(pElement);
      addManagedElement(FARM_SET, pElement, false);
    } else {
      for (RuleEntry entry : getRuleEntries()) {
        if (entry.getRule().isValid(pElement)) {
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
        for (RuleEntry entry : getRuleEntries()) {
          if (entry.getRule().isValid(pElement)) {
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
      Hashtable<FightReport, String> newGroups = new Hashtable<>();
      for (ManageableType t : getAllElements(pGroup)) {
        FightReport report = (FightReport) t;
        for (RuleEntry entry : getRuleEntries()) {
          if (entry.getRule().isValid(report)) {
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
  public void loadElements(String pFile) {
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return;
    }
    invalidate();
    initialize();
    rules.clear();
    File reportFile = new File(pFile);
    try {
      if (reportFile.exists()) {
        if (logger.isDebugEnabled()) {
          logger.debug("Reading reports from '" + pFile + "'");
        }
        try {
          Document d = JaxenUtils.getDocument(reportFile);
          for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//reportSets/reportSet")) {
            String setKey = e.getAttributeValue("name");
            setKey = URLDecoder.decode(setKey, "UTF-8");
            if (logger.isDebugEnabled()) {
              logger.debug("Loading report set '" + setKey + "'");
            }
            addGroup(setKey);
            for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "reports/report")) {
              FightReport r = new FightReport();
              r.loadFromXml(e1);
              addManagedElement(setKey, r);
            }
          }
          logger.debug("Reports successfully loaded");
          
          for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//rules/rule")) {
            RuleEntry r = new RuleEntry(e);
            rules.add(r);
          }
          logger.debug("Report Rules successfully loaded");
        } catch (Exception e) {
          logger.error("Failed to load Reports", e);
        }
      } else {
        if (logger.isInfoEnabled()) {
          logger.info("Reports file not found under '" + pFile + "'");
        }
      }
    } finally {
      revalidate();
    }
  }

  @Override
  public boolean importData(File pFile, String pExtension) {
    invalidate();
    boolean result = false;
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return false;
    }
    logger.debug("Importing reports");
    try {
      Document d = JaxenUtils.getDocument(pFile);
      for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//reportSets/reportSet")) {
        String setKey = e.getAttributeValue("name");
        setKey = URLDecoder.decode(setKey, "UTF-8");
        if (pExtension != null) {
          setKey += "_" + pExtension;
        }
        addGroup(setKey);
        if (logger.isDebugEnabled()) {
          logger.debug("Loading report set '" + setKey + "'");
        }

        for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "reports/report")) {
          FightReport r = new FightReport();
          r.loadFromXml(e1);
          addManagedElement(setKey, r);
        }
      }

      logger.debug("Reports imported successfully");
      result = true;
    } catch (Exception e) {
      logger.error("Failed to import reports", e);
    } finally {
      revalidate(true);
    }
    return result;
  }

  @Override
  public String getExportData(List<String> pGroupsToExport) {
    logger.debug("Generating report export data");

    StringBuilder b = new StringBuilder();
    b.append("<reportSets>\n");
    for (String set : pGroupsToExport) {
      b.append("<reportSet name=\"").append(set).append("\">\n");
      ManageableType[] elements = getAllElements(set).toArray(new ManageableType[getAllElements(set).size()]);
      b.append("<reports>\n");
      for (ManageableType t : elements) {
        b.append(t.toXml()).append("\n");
      }
      b.append("</reports>\n");
      b.append("</reportSet>\n");
    }
    b.append("</reportSets>\n");
    logger.debug("Export data generated successfully");
    return b.toString();
  }

  @Override
  public void saveElements(String pFile) {
    if (pFile == null) {
      logger.error("File argument is 'null'");
      return;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Writing reports to '" + pFile + "'");
    }
    try {
      StringBuilder b = new StringBuilder();
      b.append("<reportSets>\n");
      Iterator<String> setKeys = getGroupIterator();
      while (setKeys.hasNext()) {
        String set = setKeys.next();
        if (!set.equals(FARM_SET) || !GlobalOptions.getProperties().getBoolean("delete.farm.reports.on.exit")) {
          b.append("<reportSet name=\"").append(set).append("\">\n");
          b.append("<reports>\n");
          for (ManageableType t : getAllElements(set)) {
            b.append(t.toXml()).append("\n");
          }
          b.append("</reports>\n");
          b.append("</reportSet>\n");
        }
      }
      b.append("</reportSets>");
      
      b.append("<rules>");
      for(RuleEntry r: rules) {
          b.append(r.getRule().toXml());
      }
      b.append("</rules>");
      //writing data to file
      FileWriter w = new FileWriter(pFile);
      w.write(b.toString());
      w.flush();
      w.close();
      logger.debug("Reports successfully saved");
    } catch (Exception e) {
      if (!new File(pFile).getParentFile().exists()) {
        //server directory obviously does not exist yet
        //this should only happen at the first start
        logger.info("Ignoring error, server directory does not exists yet");
      } else {
        logger.error("Failed to save reports", e);
      }
    }
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

  public static class RuleEntry {

    private ReportRuleInterface rule = null;
    private String targetSet = null;

    public RuleEntry(ReportRuleInterface pRule, String pTargetSet) {
      rule = pRule;
      targetSet = pTargetSet;
    }

    private RuleEntry(Element e) {
        //TODO create function
    }

    public void setRule(ReportRuleInterface rule) {
      this.rule = rule;
    }

    public ReportRuleInterface getRule() {
      return rule;
    }

    public void setTargetSet(String targetSet) {
      this.targetSet = targetSet;
    }

    public String getTargetSet() {
      return targetSet;
    }

    @Override
    public String toString() {
      return rule.getStringRepresentation() + " -> " + targetSet;
    }
  }
}
