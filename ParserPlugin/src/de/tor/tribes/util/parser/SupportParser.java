/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.NotifierFrame;
import de.tor.tribes.util.EscapeChars;
import de.tor.tribes.util.SilentParserInterface;
import de.tor.tribes.util.troops.SupportVillageTroopsHolder;
import de.tor.tribes.util.troops.TroopsManager;
import java.util.Hashtable;
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

                if (supportSender != null) {
                    //remove all supports
                    SupportVillageTroopsHolder holder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(supportSender, TroopsManager.TROOP_TYPE.SUPPORT);
                    if (holder != null) {
                        //remove all supports if there are any to avoid old entries
                        holder.clearSupports();
                    }
                }
                /*//might be troop hosting village
                Village before = v;
                try {
                v = new VillageParser().parse(line).get(0);
                } catch ( Exception e ) {
                v = null;
                }
                if ( before != null ) {
                //one village has finished, update troops outside values!
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(before);
                if ( holder != null ) {
                holder.updateSupportValues();
                }
                }
                
                if ( v != null ) {
                //Hashtable<UnitHolder, Integer> own = parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.own"))).trim());
                Hashtable<UnitHolder, Integer> own = parseUnits(line.substring(line.indexOf(ParserVariableManager.getSingleton().getProperty("troops.in.village"))).trim());
                
                if ( own != null ) {
                //only add valid troop information
                VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                if ( holder == null ) {
                TroopsManager.getSingleton().addTroopsForVillageFast(v, new LinkedList<Integer>());
                holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                } else {
                holder.clearSupportTargets();
                }
                holder.setOwnTroops(own);
                //one valid information parsed
                retValue = true;
                
                
                }//end troops == null
                }//end host == null*/
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
                        holder.addOutgoingSupport(supportTarget, supportTroops);
                        SupportVillageTroopsHolder supporterHolder = (SupportVillageTroopsHolder) TroopsManager.getSingleton().getTroopsForVillage(supportTarget, TroopsManager.TROOP_TYPE.SUPPORT, true);
                        supporterHolder.addIncomingSupport(supportSender, supportTroops);
                        supportCount++;
                    }
                    /* if ( holder != null ) {
                    //remove existing supports
                    holder.clearSupportTargets();
                    }*/
                    /*	    Village supportTarget = null;
                    try {
                    supportTarget = new VillageParser().parse(line).get(0);
                    } catch ( Exception e ) {
                    supportTarget = null;
                    }
                    
                    if ( supportTarget != null ) {
                    Hashtable<UnitHolder, Integer> support = parseUnits(line.replaceAll(Pattern.quote(supportTarget.toString()), "").trim());
                    if ( support != null ) {
                    //only add valid troop information
                    if ( holder == null ) {
                    TroopsManager.getSingleton().addTroopsForVillageFast(v, new LinkedList<Integer>());
                    holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                    }
                    
                    //add support target for source village
                    holder.addSupportTarget(supportTarget);
                    //add suppporting troops to target
                    holder = TroopsManager.getSingleton().getTroopsForVillage(supportTarget);
                    if ( holder == null ) {
                    TroopsManager.getSingleton().addTroopsForVillageFast(supportTarget, new LinkedList<Integer>());
                    holder = TroopsManager.getSingleton().getTroopsForVillage(supportTarget);
                    holder.addSupport(v, support);
                    } else {
                    holder.addSupport(v, support);
                    }
                    holder.updateSupportValues();
                    }
                    }//end parsed troops == null
                    }//end host == null
                    }//end support target check
                    }
                    
                    //update troops for last found village
                    if ( v != null ) {
                    //one village has finished, update troops outside values!
                    VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(v);
                    if ( holder != null ) {
                    holder.updateSupportValues();
                    }
                    }
                    if ( retValue ) {
                    TroopsManager.getSingleton().forceUpdate();
                    }*/
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

    private static Village extractVillage(String pLine) {
        List<Village> villages = new VillageParser().parse(pLine);
        switch (villages.size()) {
            case 0:
                return null;
            case 2:
                return villages.get(1);
            default:
                return villages.get(0);
        }
    }

    private static Hashtable<UnitHolder, Integer> parseUnits(String pLine) {
        String line = pLine.replaceAll(ParserVariableManager.getSingleton().getProperty("troops.own"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops.commands"), "").replaceAll(ParserVariableManager.getSingleton().getProperty("troops"), "");
        StringTokenizer t = new StringTokenizer(line, " \t");
        int uCount = DataHolder.getSingleton().getUnits().size();
        Hashtable<UnitHolder, Integer> units = new Hashtable<UnitHolder, Integer>();
        int cnt = 0;
        while (t.hasMoreTokens()) {
            try {
                int amount = Integer.parseInt(t.nextToken());
                UnitHolder u = DataHolder.getSingleton().getUnits().get(cnt);
                units.put(u, amount);
                cnt++;
            } catch (Exception e) {
                //token with no troops
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
