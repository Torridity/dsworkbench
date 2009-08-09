/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.html;

import de.tor.tribes.util.html.*;
import de.tor.tribes.util.*;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *@TODO (DIFF) Unit type in attack export
 * @author Charon
 */
public class AttackPlanHTMLExporter {

    private static Logger logger = Logger.getLogger("AttackHTMLExporter");
    private static String HEADER = "";
    private static String FOOTER = "";
    private static String BLOCK = "";
    private static boolean TEMPLATE_ERROR = false;
    //header variables
    private static final String CREATOR = "\\$CREATOR";
    private static final String SERVER = "\\$SERVER";
    private static final String PLANNAME = "\\$PLANNAME";
    private static final String ATTACK_COUNT = "\\$ATTACK_COUNT";
    //block variables
    private static final String ID = "\\$ID";
    private static final String TYPE = "\\$TYPE";
    private static final String UNIT = "\\$UNIT";
    private static final String DIV_CLASS = "\\$DIV_CLASS";
    private static final String SOURCE_TRIBE = "\\$SOURCE_TRIBE";
    private static final String TARGET_TRIBE = "\\$TARGET_TRIBE";
    private static final String SOURCE_ALLY = "\\$SOURCE_ALLY";
    private static final String TARGET_ALLY = "\\$TARGET_ALLY";
    private static final String SOURCE_VILLAGE = "\\$SOURCE_VILLAGE";
    private static final String TARGET_VILLAGE = "\\$TARGET_VILLAGE";
    private static final String SEND_TIME = "\\$SEND_TIME";
    private static final String ARRIVE_TIME = "\\$ARRIVE_TIME";
    private static final String PLACE = "\\$PLACE";
    //footer variables
    private static final String VERSION = "\\$VERSION";
    private static final String CREATION_DATE = "\\$CREATION_DATE";


    static {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(AttackPlanHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/attack_header.tmpl")));
            String line = "";
            while ((line = r.readLine()) != null) {
                HEADER += line + "\n";
            }
            r.close();
            r = new BufferedReader(new InputStreamReader(AttackPlanHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/attack_block.tmpl")));
            line = "";
            while ((line = r.readLine()) != null) {
                BLOCK += line + "\n";
            }
            r.close();
            r = new BufferedReader(new InputStreamReader(AttackPlanHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/attack_footer.tmpl")));
            line = "";
            while ((line = r.readLine()) != null) {
                FOOTER += line + "\n";
            }
            r.close();
        } catch (Exception e) {
            logger.error("Failed to read templates", e);
            TEMPLATE_ERROR = true;
        }
    }

    public static void doExport(File pHtmlFile, String pPlanName, List<Attack> pAttacks) {
        if (TEMPLATE_ERROR) {
            logger.warn("Skip writing HTML file due to TEMPLATE_ERROR flag");
            return;
        }
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        StringBuffer result = new StringBuffer();
        // <editor-fold defaultstate="collapsed" desc=" build header">
        String h = HEADER;
        //set creator
        Tribe user = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
        if (user != null) {
            h = h.replaceAll(CREATOR, user.toString());
        } else {
            h = h.replaceAll(CREATOR, "-");
        }
        //set planname
        if (pPlanName != null) {
            h = h.replaceAll(PLANNAME, EscapeChars.forHTML(pPlanName));
        } else {
            h = h.replaceAll(PLANNAME, "-");
        }
        //set attack count
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        h = h.replaceAll(ATTACK_COUNT, nf.format(pAttacks.size()));
        //set attack count
        String server = GlobalOptions.getSelectedServer();
        if (server != null) {
            h = h.replaceAll(SERVER, server);
        } else {
            h = h.replaceAll(SERVER, "-");
        }

        result.append(h);
        // </editor-fold>

        int cnt = 0;
        for (Attack a : pAttacks) {
            String b = BLOCK;
            if (cnt % 2 == 0) {
                b = b.replaceAll(DIV_CLASS, "odd_div");
            } else {
                b = b.replaceAll(DIV_CLASS, "even_div");
            }
            UnitHolder unit = a.getUnit();
            b = b.replaceAll(UNIT, "<img src=\"http://www.dsworkbench.de/DSWorkbench/export/" + unit.getPlainName() + ".png\">");

            switch (a.getType()) {
                case Attack.CLEAN_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://www.dsworkbench.de/DSWorkbench/export/att.png\">");
                    break;
                }
                case Attack.SNOB_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://www.dsworkbench.de/DSWorkbench/export/snob.png\">");
                    break;
                }
                case Attack.FAKE_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://www.dsworkbench.de/DSWorkbench/export/fake.png\">");
                    break;
                }
                case Attack.SUPPORT_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://www.dsworkbench.de/DSWorkbench/export/ally.png\">");
                    break;
                }
                default: {
                    b = b.replaceAll(TYPE, "-");
                    break;
                }
            }

            b = b.replaceAll(ID, Integer.toString(cnt));

            String baseURL = ServerManager.getServerURL(server) + "/";
            // <editor-fold defaultstate="collapsed" desc=" replace source tribe and ally">
            Tribe sourceTribe = a.getSource().getTribe();
            if (sourceTribe == null) {
                //tribe is null, so it is a barbarian village
                b = b.replaceAll(SOURCE_TRIBE, "Barbaren");
                b = b.replaceAll(SOURCE_ALLY, "Barbaren");
            } else {
                String tribeGuest = baseURL;
                tribeGuest += "guest.php?screen=info_player&id=" + sourceTribe.getId();
                b = b.replaceAll(SOURCE_TRIBE, "<a href=\"" + tribeGuest + "\" target=\"_blank\">" + EscapeChars.forHTML(sourceTribe.getName()) + "</a>");
                //replace source tribe
                Ally sourceAlly = sourceTribe.getAlly();
                if (sourceAlly == null) {
                    //tribe has no ally
                    b = b.replaceAll(SOURCE_ALLY, "Kein Stamm");
                } else {
                    //ally valid
                    String allyGuest = baseURL;
                    allyGuest += "guest.php?screen=info_ally&id=" + sourceAlly.getId();
                    b = b.replaceAll(SOURCE_ALLY, "<a href=\"" + allyGuest + "\" target=\"_blank\">" + EscapeChars.forHTML(sourceAlly.getName()) + " (" + EscapeChars.forHTML(sourceAlly.getTag()) + ")" + "</a>");
                }
            }
            //replace source village
            String villageGuest = baseURL;
            villageGuest += "guest.php?screen=info_village&id=" + a.getSource().getId();
            b = b.replaceAll(SOURCE_VILLAGE, "<a href=\"" + villageGuest + "\" target=\"_blank\">" + EscapeChars.forHTML(a.getSource().getName()) + "</a>");
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" replace target tribe and ally">
            Tribe targetTribe = a.getTarget().getTribe();
            if (targetTribe == null) {
                //tribe is null, so it is a barbarian village
                b = b.replaceAll(TARGET_TRIBE, "Barbaren");
                b = b.replaceAll(TARGET_ALLY, "Barbaren");
            } else {
                String tribeGuest = baseURL;
                tribeGuest += "guest.php?screen=info_player&id=" + targetTribe.getId();
                b = b.replaceAll(TARGET_TRIBE, "<a href=\"" + tribeGuest + "\" target=\"_blank\">" + EscapeChars.forHTML(targetTribe.getName()) + "</a>");
                //replace source tribe
                Ally targetAlly = targetTribe.getAlly();
                if (targetAlly == null) {
                    //tribe has no ally
                    b = b.replaceAll(TARGET_ALLY, "Kein Stamm");
                } else {
                    //ally valid
                    String allyGuest = baseURL;
                    allyGuest += "guest.php?screen=info_ally&id=" + targetAlly.getId();
                    b = b.replaceAll(TARGET_ALLY, "<a href=\"" + allyGuest + "\" target=\"_blank\">" + EscapeChars.forHTML(targetAlly.getName()) + " (" + EscapeChars.forHTML(targetAlly.getTag()) + ")" + "</a>");
                }
            }
            //replace target village
            villageGuest = baseURL;
            villageGuest += "guest.php?screen=info_village&id=" + a.getTarget().getId();
            b = b.replaceAll(TARGET_VILLAGE, "<a href=\"" + villageGuest + "\" target=\"_blank\">" + EscapeChars.forHTML(a.getTarget().getName()) + "</a>");
            //</editor-fold>


            //replace arrive time
            String arrive = f.format(a.getArriveTime());
            b = b.replaceAll(ARRIVE_TIME, arrive);
            //replace send time
            long send = a.getArriveTime().getTime() - ((long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            b = b.replaceAll(SEND_TIME, f.format(new Date(send)));
            //replace place link
            String placeURL = baseURL + "game.php?village=";
            placeURL += a.getSource().getId() + "&screen=place&mode=command&target=" + a.getTarget().getId();
            b = b.replaceAll(PLACE, "<a href=\"" + placeURL + "\" target=\"_blank\">Versammlungsplatz</a>");
            result.append(b);
            cnt++;
        }
        // <editor-fold defaultstate="collapsed" desc=" build footer">
        String foot = FOOTER;
        foot = foot.replaceAll(VERSION, Double.toString(Constants.VERSION) + Constants.VERSION_ADDITION);

        f = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss 'Uhr'");

        foot = foot.replaceAll(CREATION_DATE, f.format(new Date(System.currentTimeMillis())));
        result.append(foot);
        // </editor-fold>
        try {
            FileWriter w = new FileWriter(pHtmlFile);
            w.write(result.toString());
            w.flush();
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
