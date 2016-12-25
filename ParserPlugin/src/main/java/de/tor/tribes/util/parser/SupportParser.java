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
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.UnknownUnit;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.NotifierFrame;
import de.tor.tribes.util.EscapeChars;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.SupportVillageTroopsHolder;
import de.tor.tribes.util.troops.TroopsManager;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Charon
 */
public class SupportParser implements SilentParserInterface {

    /*
    [001]PICO (453|581) K40 	im Dorf	936	178	0	0	2	0	0	98	0	0	0	0	Truppen
    [060]PICO (463|576) K40 	1731	1565	0	1755	0	0	0	0	0	0	0	0
    [034]PICO (446|587) K40 	3181	3285	0	2995	0	0	0	266	0	0	0	0
    
     * 
     * 
     * [070]PICO (80|476) K40 	152	172	0	261	0	0	0	0	0	0	0	0
    [002]PICO (78|424) K40 	im Dorf	5549	4381	0	4375	2	0	0	364	0	0	0	0	Truppen
    [004]PICO (70|468) K40 	im Dorf	404	28	0	1842	2	0	0	0	0	0	0	4	Truppen
     */
    public boolean parse(String pTroopsString) {
        StringTokenizer lineTok = new StringTokenizer(pTroopsString, "\n\r");
        Village supportSender = null;
        int supportCount = 0;
        List<Village> touchedVillages = new LinkedList<Village>();
        TroopsManager.getSingleton().invalidate();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            if (line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.in.village")) > 0) {
                try {
                    supportSender = new VillageParser().parse(line).get(0);
                } catch (Exception e) {
                    supportSender = null;
                }

                if (supportSender != null && !touchedVillages.contains(supportSender)) {
                    //remove all supports
                    SupportVillageTroopsHolder holder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(supportSender, TroopsManager.TROOP_TYPE.SUPPORT);
                    if (holder != null) {
                        //remove all supports if there are any to avoid old entries
                        holder.clearSupports();
                        touchedVillages.add(supportSender);
                    }
                }
            } else {
                if (supportSender != null) {
                    //might be support target village
                    SupportVillageTroopsHolder holder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(supportSender, TroopsManager.TROOP_TYPE.SUPPORT, true);
                    Village supportTarget = null;
                    try {
                        supportTarget = new VillageParser().parse(line).get(0);
                    } catch (Exception e) {
                        supportTarget = null;
                    }

                    if (supportTarget != null) {
                        //found new support
                        Hashtable<UnitHolder, Integer> supportTroops = parseUnits(line.replaceAll(Pattern.quote(supportTarget.toString()), "").trim());

                        if (supportTroops != null) {
                            holder.addOutgoingSupport(supportTarget, supportTroops);
                            SupportVillageTroopsHolder supporterHolder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(supportTarget, TroopsManager.TROOP_TYPE.SUPPORT);
                            if (holder != null && !touchedVillages.contains(supportTarget)) {
                                //remove all supports if there are any to avoid old entries
                                holder.clearSupports();
                                touchedVillages.add(supportTarget);
                            }
                            supporterHolder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(supportTarget, TroopsManager.TROOP_TYPE.SUPPORT, true);
                            supporterHolder.addIncomingSupport(supportSender, supportTroops);
                            supportCount++;
                        } 
                    }
                }
            }
        }

        boolean result = false;
        if (supportCount > 0) {
            try {
                DSWorkbenchMainFrame.getSingleton().showSuccess("DS Workbench hat " + ((supportCount == 1) ? "eine Unterstützung " : supportCount + " Unterstützungen ") + "eingelesen");
            } catch (Exception e) {
                NotifierFrame.doNotification("DS Workbench hat " + ((supportCount == 1) ? "eine Unterstützung " : supportCount + " Unterstützungen ") + "eingelesen", NotifierFrame.NOTIFY_INFO);
            }
            result = true;
        }
        TroopsManager.getSingleton().revalidate(result);
        return result;
    }

    private static Hashtable<UnitHolder, Integer> parseUnits(String pLine) {
        String line = pLine.replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops.commands"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops"), "");
        // System.out.println("Line after: " + line);
        StringTokenizer t = new StringTokenizer(line, " \t");

        int uCount = DataHolder.getSingleton().getUnits().size();
        if (!DataHolder.getSingleton().getUnitByPlainName("militia").equals(UnknownUnit.getSingleton())) {
            //get unit count (decrease due  to militia which cannot support
            uCount -= 1;
        }
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        int cnt = 0;
        while (t.hasMoreTokens()) {
            String next = t.nextToken();
            try {
                int amount = Integer.parseInt(next);
                UnitHolder u = DataHolder.getSingleton().getUnits().get(cnt);
                //  System.out.println("Put " + u + " - " + amount);
                units.put(u, amount);
                cnt++;
            } catch (Exception e) {
                //token with no troops
                //  System.out.println("Invalid token: " + next);
            }
        }
        if (cnt != uCount) {
            //invalid troops line
            return null;
        }
        return units;
    }

    public static void main(String[] args) {
        /*
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
        String s = " 003 | Spitfire (471|482) K44\n" +
        "eigene	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Befehle\n" +
        "im Dorf	2500	1500	0	1964	500	0	0	1396	0	0	0	0	Truppen\n" +
        "auswärts	0	0	0	0	0	0	0	0	0	0	0	0\n" +
        "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle\n" +
        "2Fast4You (475|480) K44\n" +
        "eigene	600	500	0	0	134	0	0	354	0	0	0	1	Befehle\n" +
        "im Dorf	600	500	0	0	134	0	0	354	0	0	0	1	Truppen\n" +
        "auswärts	4400	3000	0	3000	66	0	0	1046	0	0	0	0\n" +
        "unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle\n";
        
        
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        SupportParser.parse(data);
        } catch (Exception e) {
        e.printStackTrace();
        }
        
         */

        String s = "Rohan 015 (67|381) K30Dorf-ÜbersichtAuf Karte zentrierenTruppen schicken 	8100	8107	0	500	0	500	0	0	0 ";
        String v = "Rohan 015 (67|381)";
        System.out.println("Before" + v);
        v = EscapeChars.forRegex(v);
        System.out.println("After: " + v);
        System.out.println("SB " + s);
        System.out.println(s.replaceAll(v, ""));

        /*  String token = "(120|192)";
        System.out.println(token.matches("\\(*[0-9]{1,3}\\|[0-9]{1,3}\\)*"));
        token = "(12:23:12)";
        System.out.println(token.matches("\\(*[0-9]{1,2}\\:[0-9]{1,2}\\:[0-9]{1,2}\\)*"));
         */

        // TroopsParser.parse(pTroopsString);
    }
    /*
    kirscheye3	435|447 FaNtAsY wOrLd ... <3	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:06:46
    02.10.08 23:41:33
    Torridity	437|445 FaNtAsY wOrLd ... 10	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:26:00
    02.10.08 23:41:33
    Torridity	438|445 Barbarendorf (12)	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:16:57
    02.10.08 23:41:33
    Torridity	439|445 Barbarendorf (13)	Schwere Kavallerie	Torridity	436|444 FaNtAsY wOrLd ... 12	02.10.08 23:06:46
    02.10.08 23:41:33
    
     */
    /*
    LGK88 (1) (458|465) K44  
    eigene	0	0	6000	0	2300	0	300	50	0	Befehle
    im Dorf	0	0	6000	0	2300	0	300	50	0	0	Truppen
    auswärts	0	0	0	0	0	0	0	0	0	0	0	0
    unterwegs	0	0	0	0	0	0	0	0	0	0	0	0	Befehle 
     */
}
