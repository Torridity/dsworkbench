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
package de.tor.tribes.util.roi;

import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class ROIManager {

    private static Logger logger = Logger.getLogger("ROIManager");
    private static ROIManager SINGLETON = null;
    private List<String> rois = null;

    public static synchronized ROIManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ROIManager();
        }
        return SINGLETON;
    }

    ROIManager() {
        rois = new LinkedList<>();
    }

    /**Load ROIs for server from file
     * @param pFile Source file
     */
    public void loadROIsFromFile(String pFile) {
        rois.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        File roiFile = new File(pFile);
        if (roiFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading markers from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(roiFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//rois/roi")) {
                    try {
                        String text = URLDecoder.decode(e.getTextTrim(), "UTF-8");
                        rois.add(text);
                    } catch (Exception inner) {
                        //ignored, marker invalid
                    }
                }
                logger.debug("ROIs successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load ROIs", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("ROI file not found under '" + pFile + "'");
            }
        }
    }

    /**
     * Write ROIs for server to file
     * @param pFile Target file
     */
    public void saveROIsToFile(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing ROIs to '" + pFile + "'");
        }
        try {

            StringBuilder b = new StringBuilder();
            b.append("<rois>\n");

            for (String r : rois) {
                String text = URLEncoder.encode(r, "UTF-8");
                b.append("<roi>" + text + "</roi>\n");
            }
            b.append("</rois>");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("ROIs successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save ROIs", e);
            }
        }
    }

    /** Get all ROIs
     * @return String[] List of ROIs
     */
    public String[] getROIs() {
        return rois.toArray(new String[]{});
    }

    /**Check if ROI exists or not
     * @param pRoi Name of the ROI [NAME (X|Y)]
     * @return TRUE if ROI exists
     */
    public boolean containsROI(String pRoi) {
        return rois.contains(pRoi);
    }

    /**Add a new ROI to a specific position
     * @param pIdx Position of ROI
     * @param pRoi Name of the ROI
     */
    public void addROI(int pIdx, String pRoi) {
        if (pIdx > rois.size() - 1) {
            rois.add(pRoi);
        } else {
            rois.add(pIdx, pRoi);
        }
    }

    /** Remove a ROI
     * @param pRoi Name of the ROI to remove
     */
    public void removeROI(String pRoi) {
        rois.remove(pRoi);
    }
}
