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
package de.tor.tribes.util.html;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.util.*;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.Attack;
import de.tor.tribes.types.StandardAttack;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.util.attack.StandardAttackManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;

/**
 * @author Charon
 */
public class AttackPlanHTMLExporter {

    private static Logger logger = Logger.getLogger("AttackHTMLExporter");
    private static String HEADER = "";
    private static String FOOTER = "";
    private static String BLOCK = "";
    private static boolean TEMPLATE_ERROR = false;
    //header and footer variables
    private static final String CREATOR = "\\$CREATOR";
    private static final String SERVER = "\\$SERVER";
    private static final String PLANNAME = "\\$PLANNAME";
    private static final String ATTACK_COUNT = "\\$ATTACK_COUNT";
    private static final String VERSION = "\\$VERSION";
    private static final String CREATION_DATE = "\\$CREATION_DATE";
    //block variables
    private static final String ID = "\\$ID";
    private static final String DIV_CLASS = "\\$DIV_CLASS";
    private static final String TYPE = "\\$TYPE";
    private static final String UNIT = "\\$UNIT";
    private static final String SEND_TIME = "\\$SEND_TIME";
    private static final String ARRIVE_TIME = "\\$ARRIVE_TIME";
    private static final String PLACE = "\\$PLACE";
    //source variables
    private static final String SOURCE_PLAYER_LINK = "\\$SOURCE_PLAYER_LINK";
    private static final String SOURCE_PLAYER_NAME = "\\$SOURCE_PLAYER_NAME";
    private static final String SOURCE_ALLY_LINK = "\\$SOURCE_ALLY_LINK";
    private static final String SOURCE_ALLY_NAME = "\\$SOURCE_ALLY_NAME";
    private static final String SOURCE_ALLY_TAG = "\\$SOURCE_ALLY_TAG";
    private static final String SOURCE_VILLAGE_LINK = "\\$SOURCE_VILLAGE_LINK";
    private static final String SOURCE_VILLAGE_NAME = "\\$SOURCE_VILLAGE_NAME";
    private static final String SOURCE_VILLAGE_COORD = "\\$SOURCE_VILLAGE_COORD";
    //target variables
    private static final String TARGET_PLAYER_LINK = "\\$TARGET_PLAYER_LINK";
    private static final String TARGET_PLAYER_NAME = "\\$TARGET_PLAYER_NAME";
    private static final String TARGET_ALLY_LINK = "\\$TARGET_ALLY_LINK";
    private static final String TARGET_ALLY_NAME = "\\$TARGET_ALLY_NAME";
    private static final String TARGET_ALLY_TAG = "\\$TARGET_ALLY_TAG";
    private static final String TARGET_VILLAGE_LINK = "\\$TARGET_VILLAGE_LINK";
    private static final String TARGET_VILLAGE_NAME = "\\$TARGET_VILLAGE_NAME";
    private static final String TARGET_VILLAGE_COORD = "\\$TARGET_VILLAGE_COORD";

    static {
        loadCustomTemplate();
    }

    public static void loadCustomTemplate() {
        try {
            HEADER = "";
            BLOCK = "";
            FOOTER = "";

            String header = GlobalOptions.getProperty("attack.template.header");
            String block = GlobalOptions.getProperty("attack.template.block");
            String footer = GlobalOptions.getProperty("attack.template.footer");
            if (header == null) {
                header = "ThisFileDoesNotExist";
            }
            if (block == null) {
                block = "ThisFileDoesNotExist";
            }
            if (footer == null) {
                footer = "ThisFileDoesNotExist";
            }
            File fHeader = new File(header);
            File fBlock = new File(block);
            File fFooter = new File(footer);

            BufferedReader r = null;
            if (!fHeader.exists()) {
                r = new BufferedReader(new InputStreamReader(AttackPlanHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/attack_header.tmpl")));
            } else {
                r = new BufferedReader(new InputStreamReader(new FileInputStream(header)));
            }

            String line = "";
            while ((line = r.readLine()) != null) {
                HEADER += line + "\n";
            }
            r.close();

            if (!fBlock.exists()) {
                r = new BufferedReader(new InputStreamReader(AttackPlanHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/attack_block.tmpl")));
            } else {
                r = new BufferedReader(new InputStreamReader(new FileInputStream(block)));
            }
            line = "";
            while ((line = r.readLine()) != null) {
                BLOCK += line + "\n";
            }
            r.close();

            if (!fFooter.exists()) {
                r = new BufferedReader(new InputStreamReader(AttackPlanHTMLExporter.class.getResourceAsStream("/de/tor/tribes/tmpl/attack_footer.tmpl")));
            } else {
                r = new BufferedReader(new InputStreamReader(new FileInputStream(footer)));
            }
            line = "";
            while ((line = r.readLine()) != null) {
                FOOTER += line + "\n";
            }
            r.close();
        } catch (Exception e) {
            logger.error("Failed to read custom templates. Switch to default template.", e);
            loadDefaultTemplate();
        }
    }

    private static void loadDefaultTemplate() {
        try {
            HEADER = "";
            BLOCK = "";
            FOOTER = "";
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
        StringBuilder result = new StringBuilder();
        //append header
        result.append(replaceHeadFootVariables(HEADER, pPlanName, pAttacks));

        int cnt = 0;
        for (Attack a : pAttacks) {
            String b = BLOCK;
            // <editor-fold defaultstate="collapsed" desc="Replace DIV-IDs">
            if (cnt % 2 == 0) {
                b = b.replaceAll(DIV_CLASS, "odd_div");
            } else {
                b = b.replaceAll(DIV_CLASS, "even_div");
            }
            b = b.replaceAll(ID, Integer.toString(cnt));
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Replace Unit Icons">
            UnitHolder unit = a.getUnit();
            b = b.replaceAll(UNIT, "<img src=\"http://torridity.de/tools/res/" + unit.getPlainName() + ".png\">");

            switch (a.getType()) {
                case Attack.CLEAN_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://torridity.de/tools/res/att.png\">");
                    break;
                }
                case Attack.SNOB_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://torridity.de/tools/res/snob.png\">");
                    break;
                }
                case Attack.FAKE_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://torridity.de/tools/res/fake.png\">");
                    break;
                }
                case Attack.FAKE_DEFF_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://torridity.de/tools/res/def_fake.png\">");
                    break;
                }
                case Attack.SUPPORT_TYPE: {
                    b = b.replaceAll(TYPE, "<img src=\"http://torridity.de/tools/res/ally.png\">");
                    break;
                }
                default: {
                    b = b.replaceAll(TYPE, "-");
                    break;
                }
            }
            // </editor-fold>

            String baseURL = ServerManager.getServerURL(GlobalOptions.getSelectedServer()) + "/";

            // <editor-fold defaultstate="collapsed" desc=" replace source tribe and ally">
            Tribe sourceTribe = a.getSource().getTribe();
            String sourceTribeName = "";
            String sourceTribeLink = "";
            String sourceAllyName = "";
            String sourceAllyTag = "";
            String sourceAllyLink = "";
            String sourceVillageName = "";
            String sourceVillageCoord = "";
            String sourceVillageLink = "";

            if (sourceTribe == null) {
                //tribe is null, so it is a barbarian village
                sourceTribeName = "Barbaren";
                sourceAllyName = "Barbaren";
            } else {
                sourceTribeLink = baseURL;
                sourceTribeLink += "guest.php?screen=info_player&id=" + sourceTribe.getId();
                sourceTribeName = sourceTribe.getName();

                //replace source tribe
                Ally sourceAlly = sourceTribe.getAlly();
                if (sourceAlly == null) {
                    //tribe has no ally
                    sourceAllyName = "Kein Stamm";
                } else {
                    //ally valid
                    sourceAllyName = sourceAlly.getName();
                    sourceAllyTag = sourceAlly.getTag();
                    sourceAllyLink = baseURL;
                    sourceAllyLink += "guest.php?screen=info_ally&id=" + sourceAlly.getId();
                }
            }
            //replace source village
            sourceVillageLink = baseURL;
            sourceVillageLink += "guest.php?screen=info_village&id=" + a.getSource().getId();
            sourceVillageName = a.getSource().getFullName();
            sourceVillageCoord = a.getSource().getCoordAsString();

            //replace values
            b = b.replaceAll(SOURCE_PLAYER_NAME, Matcher.quoteReplacement(sourceTribeName));
            b = b.replaceAll(SOURCE_PLAYER_LINK, sourceTribeLink);
            b = b.replaceAll(SOURCE_ALLY_NAME, Matcher.quoteReplacement(sourceAllyName));
            b = b.replaceAll(SOURCE_ALLY_TAG, Matcher.quoteReplacement(sourceAllyTag));
            b = b.replaceAll(SOURCE_ALLY_LINK, sourceAllyLink);
            b = b.replaceAll(SOURCE_VILLAGE_NAME, Matcher.quoteReplacement(sourceVillageName));
            b = b.replaceAll(SOURCE_VILLAGE_COORD, sourceVillageCoord);
            b = b.replaceAll(SOURCE_VILLAGE_LINK, sourceVillageLink);

            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc=" replace target tribe and ally">
            Tribe targetTribe = a.getTarget().getTribe();
            String targetTribeName = "";
            String targetTribeLink = "";
            String targetAllyName = "";
            String targetAllyTag = "";
            String targetAllyLink = "";
            String targetVillageName = "";
            String targetVillageCoord = "";
            String targetVillageLink = "";

            if (targetTribe == null) {
                //tribe is null, so it is a barbarian village
                targetTribeName = "Barbaren";
                targetAllyName = "Barbaren";
            } else {
                targetTribeLink = baseURL;
                targetTribeLink += "guest.php?screen=info_player&id=" + targetTribe.getId();
                targetTribeName = targetTribe.getName();

                //replace source tribe
                Ally targetAlly = targetTribe.getAlly();
                if (targetAlly == null) {
                    //tribe has no ally
                    targetAllyName = "Kein Stamm";
                } else {
                    //ally valid
                    targetAllyName = targetAlly.getName();
                    targetAllyTag = targetAlly.getTag();
                    targetAllyLink = baseURL;
                    targetAllyLink += "guest.php?screen=info_ally&id=" + targetAlly.getId();
                }
            }
            //replace source village
            targetVillageLink = baseURL;
            targetVillageLink += "guest.php?screen=info_village&id=" + a.getTarget().getId();
            targetVillageName = a.getTarget().getFullName();
            targetVillageCoord = a.getTarget().getCoordAsString();

            //replace values
            b = b.replaceAll(TARGET_PLAYER_NAME, Matcher.quoteReplacement(targetTribeName));
            b = b.replaceAll(TARGET_PLAYER_LINK, targetTribeLink);
            b = b.replaceAll(TARGET_ALLY_NAME, Matcher.quoteReplacement(targetAllyName));
            b = b.replaceAll(TARGET_ALLY_TAG, Matcher.quoteReplacement(targetAllyTag));
            b = b.replaceAll(TARGET_ALLY_LINK, targetAllyLink);
            b = b.replaceAll(TARGET_VILLAGE_NAME, Matcher.quoteReplacement(targetVillageName));
            b = b.replaceAll(TARGET_VILLAGE_COORD, targetVillageCoord);
            b = b.replaceAll(TARGET_VILLAGE_LINK, targetVillageLink);

            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Replace times and place URL">
            //replace arrive time
            String arrive = f.format(a.getArriveTime());
            b = b.replaceAll(ARRIVE_TIME, arrive);
            //replace send time
            long send = a.getArriveTime().getTime() - ((long) DSCalculator.calculateMoveTimeInSeconds(a.getSource(), a.getTarget(), a.getUnit().getSpeed()) * 1000);
            b = b.replaceAll(SEND_TIME, f.format(new Date(send)));
            //replace place link
            String placeURL = baseURL + "game.php?village=";
            int uvID = GlobalOptions.getSelectedProfile().getUVId();
            if (uvID >= 0) {
                placeURL = baseURL + "game.php?t=" + uvID + "&village=";
            }
            placeURL += a.getSource().getId() + "&screen=place&mode=command&target=" + a.getTarget().getId();

            placeURL += "&type=0";

            StandardAttack stdAttack = StandardAttackManager.getSingleton().getElementByIcon(a.getType());
            for (UnitHolder u : DataHolder.getSingleton().getUnits()) {
                int amount = 0;
                if (stdAttack != null) {
                    amount = stdAttack.getAmountForUnit(u, a.getSource());
                }
                placeURL += "&" + u.getPlainName() + "=" + amount;
            }

            b = b.replaceAll(PLACE, placeURL);
            // </editor-fold>

            result.append(b);
            cnt++;
        }

        //append footer
        result.append(replaceHeadFootVariables(FOOTER, pPlanName, pAttacks));
        try {
            FileWriter w = new FileWriter(pHtmlFile);
            w.write(result.toString());
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed writing HTML file", e);
        }
    }

    private static String replaceHeadFootVariables(String pBlock, String pPlanName, List<Attack> pAttacks) {
        String result = pBlock;
        //set creator
        Tribe user = GlobalOptions.getSelectedProfile().getTribe();
        if (user != null) {
            result = result.replaceAll(CREATOR, Matcher.quoteReplacement(user.getName()));
        } else {
            result = result.replaceAll(CREATOR, "-");
        }
        //set planname
        if (pPlanName != null) {
            result = result.replaceAll(PLANNAME, EscapeChars.forHTML(pPlanName));
        } else {
            result = result.replaceAll(PLANNAME, "-");
        }
        //set attack count
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        result = result.replaceAll(ATTACK_COUNT, nf.format(pAttacks.size()));
        //set attack count
        String server = GlobalOptions.getSelectedServer();
        if (server != null) {
            result = result.replaceAll(SERVER, server);
        } else {
            result = result.replaceAll(SERVER, "-");
        }
        //replace version
        result = result.replaceAll(VERSION, Double.toString(Constants.VERSION) + Constants.VERSION_ADDITION);
        //replace creation date
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm:ss 'Uhr'");
        result = result.replaceAll(CREATION_DATE, f.format(new Date(System.currentTimeMillis())));

        return result;
    }
    
    public static void main(String[] args) {
        String test = "%P%";
        System.out.println(test.replaceAll("%P%", Matcher.quoteReplacement("$test$")));
    }
}
