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
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.village.KnownVillage;
import de.tor.tribes.util.village.KnownVillageManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author extremeCrazyCoder
 */
public class BuildingParser implements SilentParserInterface {
    private static Logger logger = LogManager.getLogger("MerchantParser");

    @Override
    public boolean parse(String pData) {
        StringTokenizer lineTok = new StringTokenizer(pData, "\n\r");
        Map<Village, int[]> buildingValues = new HashMap<>();
        List<Integer> availableBuildings = new ArrayList<>();
        for(int i = 0; i < Constants.BUILDING_NAMES.length; i++) {
            //Filter Buildings that are not availaible at this World
            switch (Constants.BUILDING_NAMES[i]) {
                case "church":
                    if(ServerSettings.getSingleton().isChurch()) {
                        availableBuildings.add(i);
                        availableBuildings.add(-2); //placeholder for first Church
                    }
                    break;
                case "watchtower":
                    if(ServerSettings.getSingleton().isWatchtower())
                        availableBuildings.add(i);
                    break;
                case "statue":
                    if(!DataHolder.getSingleton().getUnitByPlainName("knight").equals(UnknownUnit.getSingleton()))
                        availableBuildings.add(i);
                    break;
                case "hide":
                    if(ServerSettings.getSingleton().isHaulActive())
                        availableBuildings.add(i);
                    break;
                default:
                    availableBuildings.add(i);
                    break;
            }
        }

        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            logger.debug("Parsing line '" + line + "'");
            Village v = VillageParser.parseSingleLine(line);
            if(v == null) continue;
            logger.debug("Got village '" + v + "'");
            
            String split[] = line.split("\t");
            if(split.length < 2 + availableBuildings.size()) continue;
            
            try {
                int[] levels = new int[availableBuildings.size()];
                for(int i = 0; i < levels.length; i++) {
                    levels[i] = Integer.parseInt(split[i + 3].trim());
                }
                
                if(logger.isDebugEnabled()) {
                    StringBuilder s = new StringBuilder();
                    s.append("Gefunden:");
                    for(int i = 0; i < levels.length; i++) {
                        s.append(" ").append(levels[i]);
                    }
                    logger.debug(s);
                }
                
                buildingValues.put(v, levels);
            } catch (Exception e) {
                logger.debug("Failed to parse building line", e);
            }
        }
        
        for(Map.Entry<Village, int[]> levels: buildingValues.entrySet()) {
            KnownVillage v = KnownVillageManager.getSingleton().getKnownVillage(levels.getKey());
            for(int i = 0; i < levels.getValue().length; i++) {
                if(availableBuildings.get(i) > 0) {
                    v.setBuildingLevelById(availableBuildings.get(i), levels.getValue()[i]);
                }
                //special case handling
                else if(availableBuildings.get(i) == -2) {
                    //first church
                    if(levels.getValue()[i] > 0) {
                        v.setBuildingLevelByName("church", 2);
                    }
                }
            }
        }
        
        return !buildingValues.isEmpty();
    }
}
