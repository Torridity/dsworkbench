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
import java.io.FileWriter;
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
            for (Element e : (List<Element>) JDomUtils.getNodes(d, "splits/split")) {
                String name = e.getAttributeValue("name");
                TroopAmountFixed amount = new TroopAmountFixed(e);
                pTarget.put(name, amount);
            }
        } catch(Exception e) {
            logger.info("Failed to load Splits", e);
        }
    }

    public static void saveSplitSets(HashMap<String, TroopAmountFixed> pSource) {
        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        File filterFile = new File(profileDir + "/splits.xml");
        
        StringBuilder b = new StringBuilder();
        b.append("<data><splits>\n");
        
        for(String key: pSource.keySet()) {
            b.append("<split name=\"");
            b.append(key).append("\" ");
            b.append(pSource.get(key).toXml());
            b.append(" />\n");
        }
        b.append("</splits></data>\n");
        
        try (FileWriter w = new FileWriter(filterFile)) {
            w.write(b.toString());
            w.flush();
        } catch (Exception e) {
            logger.error("Failed to write split sets", e);
        }
    }
}
