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
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GenericParserInterface;
import de.tor.tribes.util.ServerSettings;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Charon
 */
public class VillageParser implements GenericParserInterface<Village> {

    @Override
    public List<Village> parse(String pVillagesString) {
        List<Village> villages = new LinkedList<>();
        if (pVillagesString == null) {
            return villages;
        }
        StringTokenizer lines = new StringTokenizer(pVillagesString, "\n\r");
        while (lines.hasMoreTokens()) {
            if (ServerSettings.getSingleton().getCoordType() != 2) {
                StringTokenizer t = new StringTokenizer(lines.nextToken(), " []\t");
                Village lastLineVillage = null;
                while (t.hasMoreTokens()) {
                    try {
                        String token = t.nextToken();
                        if (token.matches("\\(*[0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2}\\)*")) {
                            token = token.replaceAll("\\(", "").replaceAll("\\)", "");
                            String[] split = token.split(":");
                            int[] coord = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                            Village v = null;
                            if (DataHolder.getSingleton().isDataValid()) {
                                v = DataHolder.getSingleton().getVillages()[coord[0]][coord[1]];
                            }
                            if (v != null) {
                                lastLineVillage = v;
                            }
                        }
                    } catch (Exception e) {
                        //skip token
                    }
                }
                if (lastLineVillage != null) {
                    if (!villages.contains(lastLineVillage)) {
                        villages.add(lastLineVillage);
                    }
                }
            } else {
                StringTokenizer t = new StringTokenizer(lines.nextToken(), " []\t");
                Village lastLineVillage = null;
                while (t.hasMoreTokens()) {
                    
                    try {
                        String token = t.nextToken();
                        if (token.matches("\\(*[0-9]{1,3}\\|[0-9]{1,3}\\)*")) {
                            token = token.replaceAll("\\(", "").replaceAll("\\)", "");
                            String[] split = token.split("\\|");
                            // Village v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                            Village v = null;
                            if (DataHolder.getSingleton().isDataValid()) {
                                v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                            }
                            if (v != null) {
                                lastLineVillage = v;
                            }
                        }
                    } catch (Exception e) {
                        //skip token
                    }
                }
                if (lastLineVillage != null) {
                    if (!villages.contains(lastLineVillage)) {
                        villages.add(lastLineVillage);
                    }
                }
            }
        }
        return villages;
    }
}
