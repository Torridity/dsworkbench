/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.parser;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.ServerSettings;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Jejkal
 */
public class GroupParser {

    private static Logger logger = Logger.getLogger("GroupParser");
    /*
    (09) Sunset Beach (459|468) K44  	2	Fertig; Off	» bearbeiten
    )=-g-town-=( (469|476) K44  	2	Fertig; Off	» bearbeiten
    003 | Spitfire (471|482) K44  	2	Deff; Fertig	» bearbeiten
    024 | Spitfire (470|482) K44  	2	Fertig; Off	» bearbeiten
    053 | Spitfire (472|482) K44  	2	Fertig; Off	» bearbeiten
    2Fast4You (475|480) K44  	2	Deff; Fertig	» bearbeiten
    Aberdeen - Eastside (497|469) K44  	2	Off; Truppenbau	» bearbeiten
     */

    public static boolean parse(String pGroupsString) {
        StringTokenizer lineTok = new StringTokenizer(pGroupsString, "\n\r");

        Hashtable<String, List<Village>> groups = new Hashtable<String, List<Village>>();
        while (lineTok.hasMoreElements()) {
            //parse single line for village
            String line = lineTok.nextToken();
            //german and suisse
            if (line.trim().endsWith("bearbeiten") || line.trim().endsWith("bearbeite")) {
                System.out.println(line);
                try {
                    //tokenize line by tab
                    StringTokenizer elemTok = new StringTokenizer(line.trim(), "\t");

                    String villageToken = elemTok.nextToken().trim();
                    System.out.println("  Village: " + villageToken);
                    String groupCountToken = elemTok.nextToken().trim();
                    System.out.println(" Group: " + groupCountToken);
                    String groupsToken = null;
                    try {
                        //test group count
                        Integer.parseInt(groupCountToken);
                        System.out.println(" PARSED!");
                        //group count found, next token must be groups
                        groupsToken = elemTok.nextToken().trim();
                        System.out.println(" Groups:" + groupsToken);
                    } catch (Exception e) {
                        //group count not found (Google Chrome uses 2 tabs after village)
                        //take next tokes as group count
                        e.printStackTrace();
                        groupCountToken = elemTok.nextToken().trim();
                        groupsToken = elemTok.nextToken().trim();
                    }


                    Village v = null;
                    try {
                        String coord = villageToken.substring(villageToken.lastIndexOf("(") + 1, villageToken.lastIndexOf(")"));
                        if (ServerSettings.getSingleton().getCoordType() != 2) {
                            String[] split = coord.trim().split("[(\\:)]");
                            int[] xy = DSCalculator.hierarchicalToXy(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                            v = DataHolder.getSingleton().getVillages()[xy[0]][xy[1]];
                        } else {
                            String[] split = coord.trim().split("[(\\|)]");
                            v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[0])][Integer.parseInt(split[1])];
                        }
                    } catch (Exception e) {
                    }
                    if (v != null) {
                        //valid line found
                        int groupCount = 0;
                        try {
                            groupCount = Integer.parseInt(groupCountToken.trim());
                        } catch (Exception e) {
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
                                        groupVillages = new LinkedList<Village>();
                                        groups.put(group, groupVillages);
                                    }
                                    groupVillages.add(v);
                                }
                            } else {
                                logger.error("Group count (" + groupCount + ") is not equal token count (" + groupsTokenizer.countTokens() + ") for token " + groupsToken);
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    //one line failed
                }
            }
        }
        if (groups.size() != 0) {
            DSWorkbenchMainFrame.getSingleton().fireGroupParserEvent(groups);
            return true;
        }
        return false;
    }

//v = DataHolder.getSingleton().getVillages()[Integer.parseInt(split[1])][Integer.parseInt(split[2])];
//next 4 lines are village
                        /*villageLines = 4;*/
//  }
    public static void main(String[] args) throws Exception {
        Transferable t = (Transferable) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        //String data = "(09) Sunset Beach (459|468) K44  	2	Fertig; Off	» bearbeiten";
        String data = (String) t.getTransferData(DataFlavor.stringFlavor);
        GroupParser.parse(data);
    }
}
