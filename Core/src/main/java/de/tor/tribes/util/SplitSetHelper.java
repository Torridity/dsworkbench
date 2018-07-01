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
package de.tor.tribes.util;

import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.util.xml.JDomUtils;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Torridity
 */
public class SplitSetHelper {

    private static Logger logger = LogManager.getLogger("SplitHelper");

    public static void loadSplitSets(HashMap<String, TroopAmountFixed> pTarget) {
        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        File filterFile = new File(profileDir + "/splits.xml");
        if (!filterFile.exists()) {
            return;
        }
        
        try {
            Document d = JDomUtils.getDocument(filterFile);
            if(loadSplitSets(pTarget, d.getRootElement(), null) < 0) {
                logger.info("Failed to load Splits inner");
            }
        } catch(Exception e) {
            logger.info("Failed to load Splits", e);
        }
    }
    
    public static int loadSplitSets(HashMap<String, TroopAmountFixed> pTarget, Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        
        try {
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "splits/split")) {
                String name = e.getAttributeValue("name");
                if (pExtension != null) {
                    name += "_" + pExtension;
                }
                TroopAmountFixed amount = new TroopAmountFixed(e);
                pTarget.put(name, amount);
                result++;
            }
        } catch(Exception e) {
            result = result * (-1) - 1;
            logger.info("Failed to load Splits", e);
        }
        
        return result;
    }
    
    public static int importData(Element pElm, String pExtension) {
        HashMap<String, TroopAmountFixed> pSource = new HashMap<>();
        loadSplitSets(pSource);
        int num = loadSplitSets(pSource, pElm, pExtension);
        saveSplitSets(pSource);
        return num;
    }

    public static void saveSplitSets(HashMap<String, TroopAmountFixed> pSource) {
        Document doc = JDomUtils.createDocument();
        doc.getRootElement().addContent(getExportData(pSource));
        
        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        JDomUtils.saveDocument(doc, profileDir + "/splits.xml");
    }
    
    public static Element getExportData() {
        HashMap<String, TroopAmountFixed> pSource = new HashMap<>();
        loadSplitSets(pSource);
        return getExportData(pSource);
    }
    
    public static Element getExportData(HashMap<String, TroopAmountFixed> pSource) {
        Element splits = new Element("splits");
        
        for(String key: pSource.keySet()) {
            Element split = pSource.get(key).toXml("split");
            split.setAttribute("name", key);
            splits.addContent(split);
        }
        return splits;
    }
}
