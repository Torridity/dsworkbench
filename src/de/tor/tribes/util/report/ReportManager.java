/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.report;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.FightReport;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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

    public static synchronized ReportManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ReportManager();
        }
        return SINGLETON;
    }

    ReportManager() {
        super(true);
    }

    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File reportFile = new File(pFile);
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
            } catch (Exception e) {
                logger.error("Failed to load Reports", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Reports file not found under '" + pFile + "'");
            }
        }
        revalidate();
    }

    @Override
    public String[] getGroups() {
        String[] groups = super.getGroups();
        Arrays.sort(groups, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if (o1.equals(DEFAULT_GROUP)) {
                    return -1;
                } else if (o2.equals(DEFAULT_GROUP)) {
                    return 1;
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
                }
            }
        });
        return groups;
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
        }
        revalidate(true);
        return result;
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        logger.debug("Generating report export data");

        StringBuilder b = new StringBuilder();
        b.append("<reportSets>\n");
        for (String set : pGroupsToExport) {
            b.append("<reportSet name=\"").append(set).append("\"\n");
            for (ManageableType t : getAllElements(set)) {
                b.append(t.toXml()).append("\n");
            }
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
                b.append("<reportSet name=\"").append(set).append("\">\n");
                b.append("<reports>\n");
                for (ManageableType t : getAllElements(set)) {
                    b.append(t.toXml()).append("\n");
                }
                b.append("</reports>\n");
                b.append("</reportSet>\n");
            }
            b.append("</reportSets>");
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
}
