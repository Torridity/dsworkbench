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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Charon
 */
public class VillageParser implements GenericParserInterface<Village> {
  private static VillageParser SINGLETON = null;

    @Override
    public List<Village> parse(String pVillagesString) {
        List<Village> villages = new LinkedList<>();
        if (pVillagesString == null || !DataHolder.getSingleton().isDataValid()) {
            return villages;
        }
        
        Pattern normal = Pattern.compile("([0-9]{1,3}\\|[0-9]{1,3})");
        Pattern hirarchical = Pattern.compile("([0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2})");
        
        StringTokenizer lines = new StringTokenizer(pVillagesString, "\n");
        while (lines.hasMoreTokens()) {
            String line = lines.nextToken();
            Village lastLineVillage = null;
            
            Matcher m = normal.matcher(line);
            while(m.find()) {
                try {
                    String[] split = m.group(1).split("\\|");
                    
                    Village v = DataHolder.getSingleton().getVillages()
                            [Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                    if(v != null)
                        lastLineVillage = v;
                } catch (Exception e) {
                    //skip token
                }
            }
            
            if (lastLineVillage == null) {
                //nothing found try other coord type
                m = hirarchical.matcher(line);
                while(m.find()) {
                    try {
                        String[] split = m.group(1).split(":");

                        int[] coord = DSCalculator.hierarchicalToXy(
                                Integer.parseInt(split[0]),
                                Integer.parseInt(split[1]),
                                Integer.parseInt(split[2]));
                        
                        Village v = DataHolder.getSingleton().getVillages()[coord[0]][coord[1]];
                        if(v != null)
                            lastLineVillage = v;
                    } catch (Exception e) {
                        //skip token
                    }
                }
            }
            
            if (lastLineVillage != null) {
                if (!villages.contains(lastLineVillage)) {
                    villages.add(lastLineVillage);
                }
            }
        }
        return villages;
    }

    public static Village parseSingleLine(String line) {
        if(SINGLETON == null) SINGLETON = new VillageParser();
        
        List<Village> results = SINGLETON.parse(line);
        if(results.isEmpty()) return null;
        
        return results.get(results.size() - 1);
    }
}
