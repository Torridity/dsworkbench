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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class SplitSetHelper {

    private static Logger logger = Logger.getLogger("SplitHelper");

    public static void loadSplitSets(Hashtable<String, Hashtable<UnitHolder, Integer>> pTarget) {
        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        File filterFile = new File(profileDir + "/splits.sav");
        if (!filterFile.exists()) {
            return;
        }

        BufferedReader r = null;

        try {
            r = new BufferedReader(new FileReader(filterFile));
            String line = "";
            while ((line = r.readLine()) != null) {
                String[] split = line.split(",");
                String name = split[0];
                Hashtable<UnitHolder, Integer> elements = new Hashtable<>();
                for (int i = 1; i < split.length; i++) {
                    String[] elemSplit = split[i].split("/");
                    elements.put(DataHolder.getSingleton().getUnitByPlainName(elemSplit[0]), Integer.parseInt(elemSplit[1]));
                }
                pTarget.put(name, elements);
            }
        } catch (Exception e) {
            logger.error("Failed to read split sets", e);
        } finally {
            try {
                r.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void saveSplitSets(Hashtable<String, Hashtable<UnitHolder, Integer>> pSource) {
        String profileDir = GlobalOptions.getSelectedProfile().getProfileDirectory();
        File filterFile = new File(profileDir + "/splits.sav");

        StringBuilder b = new StringBuilder();
        Enumeration<String> setKeys = pSource.keys();

        while (setKeys.hasMoreElements()) {
            String key = setKeys.nextElement();
            b.append(key).append(",");
            Hashtable<UnitHolder, Integer> elements = pSource.get(key);
            Enumeration<UnitHolder> keys = elements.keys();
            int cnt = 0;
            while (keys.hasMoreElements()) {

                UnitHolder unit = keys.nextElement();
                b.append(unit.getPlainName()).append("/").append(elements.get(unit));
                if (cnt < elements.size() - 1) {
                    b.append(",");
                }
                cnt++;
            }
            b.append("\n");
        }

        FileWriter w = null;
        try {
            w = new FileWriter(filterFile);
            w.write(b.toString());
            w.flush();
        } catch (Exception e) {
            logger.error("Failed to write split sets", e);
        } finally {
            try {
                w.close();
            } catch (Exception ignored) {
            }
        }
    }
}
