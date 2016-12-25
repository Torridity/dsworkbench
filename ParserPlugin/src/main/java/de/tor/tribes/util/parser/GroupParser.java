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
import de.tor.tribes.php.json.JSONObject;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.SilentParserInterface;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class GroupParser implements SilentParserInterface {

    private static Logger logger = Logger.getLogger("GroupParser");
    /*
    (09) Sunset Beach (459|468) K44  	2   1023    2323	Fertig; Off	» bearbeiten
    )=-g-town-=( (469|476) K44  	2   1234    2323	Fertig; Off	» bearbeiten
    003 | Spitfire (471|482) K44  	2   2323    2323	Deff; Fertig	» bearbeiten
    024 | Spitfire (470|482) K44  	2   2323    2323	Fertig; Off	» bearbeiten
    053 | Spitfire (472|482) K44  	2   2323    2323	Fertig; Off	» bearbeiten
    2Fast4You (475|480) K44  	2   2323    2323	Deff; Fertig	» bearbeiten
    Aberdeen - Eastside (497|469) K44  	2   2323    2323	Off; Truppenbau	» bearbeiten
     */

    private boolean parseVillageRenamerData(String pData) {
        Hashtable<String, List<Village>> mappings = new Hashtable<>();
        try {
            JSONObject sectorObject = new JSONObject(pData);
            JSONObject data = (JSONObject) sectorObject.get("id");
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String villageId = keys.next();
                String groupName = (String) data.get(villageId);

                List<Village> groups = mappings.get(groupName);
                if (groups == null) {
                    groups = new LinkedList<>();
                    mappings.put(groupName, groups);
                }
                Village v = DataHolder.getSingleton().getVillagesById().get(Integer.parseInt(villageId));
                groups.add(v);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse group info from village renamer data");
            mappings.clear();
        }

        if (!mappings.isEmpty()) {
            DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(mappings);
            return true;
        }
        return false;
    }

    public boolean parse(String pGroupsString) {
    	
        if (parseVillageRenamerData(pGroupsString)) {
            return true;
        }


        StringTokenizer lineTok = new StringTokenizer(pGroupsString, "\n\r");

        Hashtable<String, List<Village>> groups = new Hashtable<>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            //german and suisse
            if (line.trim().endsWith(ParserVariableManager.getSingleton().getProperty("groups.edit"))) {
                try {
                    //tokenize line by tab
                    StringTokenizer elemTok = new StringTokenizer(line.trim(), "\t");

                    String villageToken = elemTok.nextToken().trim();
                    String groupCountToken = elemTok.nextToken().trim();

                    String groupsToken = null;
                    try {
                        //test group count
                        if (Integer.parseInt(groupCountToken) > 20) {
                            //second token is no valid group amount...
                            throw new Exception("Too large group count (Hybrix distance script?)");
                        }

                        //group count found, next token must be groups
                        //skip version 7.0 village points token
                        elemTok.nextToken();
                        //skip version 7.1 farm space token
                        elemTok.nextToken();
                        groupsToken = elemTok.nextToken().trim();
                    } catch (Exception e) {
                        //group count not found (Google Chrome uses 2 tabs after village)
                        //take next tokes as group count
                        groupCountToken = elemTok.nextToken().trim();
                        //skip village points token
                        elemTok.nextToken();
                        //skip again...version 7.1 farm space token ^^
                        elemTok.nextToken();
                        groupsToken = elemTok.nextToken().trim();
                    }

                    Village v = new VillageParser().parse(villageToken).get(0);

                    if (v != null) {
                        //valid line found
                        int groupCount = 0;
                        try {
                            groupCount = Integer.parseInt(groupCountToken.trim());
                        } catch (Exception e) {
                            groupCount = 0;
                        }
                        if (groupCount > 0) {
                            //group number found
                            StringTokenizer groupsTokenizer = new StringTokenizer(groupsToken, ";");
                            if (groupsTokenizer.countTokens() == groupCount) {
                                //valid group names
                                while (groupsTokenizer.hasMoreTokens()) {
                                    String group = groupsTokenizer.nextToken().trim();
                                    List<Village> groupVillages = groups.get(group);
                                    if (groupVillages == null) {
                                        groupVillages = new LinkedList<>();
                                        groups.put(group, groupVillages);
                                    }
                                    groupVillages.add(v);
                                }
                            } else {
                                logger.error("Group count (" + groupCount + ") is not equal token count (" + groupsTokenizer.countTokens() + ") for token " + groupsToken);
                                throw new Exception("-ignore-");
                            }
                        }
                    }
                } catch (Exception e) {
                    //one line failed
                }
            }
        }
        if (!groups.isEmpty()) {
            DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(groups);
            return true;
        }
        return false;
    }

    public boolean parse_regex(String pGroups) {
        String villageRegEx = "(.*)[\\s]\\(([0-9]{1,3})\\|([0-9]{1,3})\\)[\\s]K([1-9]{1,2})";
        String groupCountRegEx = "([0-9]{1,2})";
        String groupRegEx = "[(.*);\\s]*(.*)\\s(»[\\s]*bearbeiten)";
        Pattern regExPattern = Pattern.compile(villageRegEx + "(.*)" + groupCountRegEx + "\\s" + groupRegEx);
        StringTokenizer lines = new StringTokenizer(pGroups, "\n");
        Hashtable<String, List<Village>> groupMap = new Hashtable<>();
        while (lines.hasMoreTokens()) {
            String newLine = lines.nextToken().trim();
            Matcher matcher = regExPattern.matcher(newLine);
            if (matcher.matches()) {
                //1 : Name
                //2 : xKoord
                //3 : yKoord
                //4 : Kont
                //5 : Misc
                //6 : GroupCount
                //7 : Groups
                //8 : end stuff

                int x = Integer.parseInt(matcher.group(2));
                int y = Integer.parseInt(matcher.group(3));
                Village groupedVillage = DataHolder.getSingleton().getVillages()[x][y];

                int groupCount = Integer.parseInt(matcher.group(6));
                String groups = matcher.group(7).trim();
                String[] singleGroups = groups.split(";");
                if (singleGroups.length == groupCount) {
                    //group count correct
                    for (String group : singleGroups) {
                        List<Village> villagesInGroup = groupMap.get(group);
                        if (villagesInGroup == null) {
                            villagesInGroup = new LinkedList<>();
                            groupMap.put(group, villagesInGroup);
                        }
                        villagesInGroup.add(groupedVillage);
                    }
                }
            }
        }



        //  Pattern pattern = Pattern.compile("Der[\\s](.*)[\\s]hat[\\s]gewonnen[\\s]?");
        //pattern = Pattern.compile("Moral:[\\s]([0-9|\\.-]{3,5})%");
        //String villageRegEx = "(.*)[\\s](\\([0-9]{1,3})\\|([0-9]{1,3}\\))[\\s]K([1-9]{1,2})";
        //pattern = Pattern.compile("Herkunft:[\\s](.*)[\\s]\\(([0-9]{1,3})\\|([0-9]{1,3})\\)[\\s]K([1-9]{1,2})");
        // pattern = Pattern.compile("Angreifer:(.*?)[\\s](.*)");
        //      pattern = Pattern.compile("Anzahl:[\\s]*([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})[\\s]([0-9]{1,})");
        //pattern = Pattern.compile("(.*)[\\s*]\\(([0-9]{1,3})\\|([0-9]{1,3})\\)[\\s*]K([1-9]{1,2})[\\s*](.*)[\\s*]eigene[\\s]*([0-9]{1,})[\\s*]Befehle[\\s*]im Dorf[\\s*]([0-9]{1,})");

        //015 R.I.P. Frankfurt Lions 01 (382|891) K83  	119.04	2	Off; Off_F	» bearbeiten

        // String groups = "[(.*);\\s]*(.*)\\s(»[\\s]*bearbeiten)";

// 015 R.I.P. Frankfurt Lions 01 (382|891) K83     2 Off; Off_F » bearbeiten
        // Pattern pattern = Pattern.compile(villageRegEx + "(.*)([0-9]{1,2})\\s" + groups);
        //  pattern = Pattern.compile(groups);
        // Matcher matcher = pattern.matcher(pGroups);//" 015 R.I.P. Frankfurt Lions 03 (383|892) K83     2 Off; Off_F » bearbeiten");

        //matcher = pattern.matcher("Moral: 100%");
        //matcher = pattern.matcher("Herkunft:    Barbarendorf (521|866) K85");
        //matcher = pattern.matcher("Angreifer:bla HeinxxBlöd (gelöscht)");
        //  matcher = pattern.matcher("Anzahl:\n    6374	3258	7960	4324	600	2200	300	1552	0	656	0	0");
        //  matcher = pattern.matcher("015 R.I.P. Frankfurt Lions 01 (382|891) K83  	119.04\neigene	0 Befehle\nim Dorf	0");//	0	5145	0	100	2113	252	0	300	100	0	0	Befehle\nim Dorf	0	0	5145	0	100	2113	252	0	300	100	0	0	Truppen\nauswärts	0	0	0	0	0	0	0	0	0	0	0	0\nunterwegs	0	0	0	0	100	0	0	0	0	0	0	0	Befehle");

        //matcher = pattern.matcher("015 R.I.P. Frankfurt Lions 01 (382|891) K83  umbenennen\neigene 	0 Befehle\nim Dorf	0");//	0	5145	0	100	2113	252	0	300	100	0	0 	Befehle\nim Dorf 	0	0	5145	0	100	2113	252	0	300	100	0	0 	Truppen\nauswärts 	0	0	0	0	0	0	0	0	0	0	0	0\nunterwegs 	0	0	0	0	100	0	0	0	0	0	0	0 	Befehle");
       /* if (matcher.matches()) {
        System.out.println("Village " + matcher.group(1) + " " + matcher.group(2) + "/" + matcher.group(3) + " K" + matcher.group(4));
        System.out.println("Groups: " + matcher.group(6));
        System.out.println("Groups: " + matcher.group(7));
        System.out.println("-------------------");
        //1 : Name
        //2 : xKoord
        //3 : yKoord
        //4 : Kont
        //5 : Misc
        //6 : GroupCount
        //7 : Groups
        //8 : end stuff
        } else {
        //no groups
        }*/
        //System.out.println("D " + (System.currentTimeMillis() - s));
        if (!groupMap.isEmpty()) {
            DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(groupMap);
            return true;
        }
        return false;
    }

//v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[1])][Integer.parseInt(split[2])];
//next 4 lines are village
                        /*villageLines = 4;*/
//  }
    public static void main(String[] args) throws Exception {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        //String data = "(09) Sunset Beach (459|468) K44  	2	Fertig; Off	» bearbeiten";
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);

        new GroupParser().parse(data);
    }
}
