/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.html;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.awt.Color;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class VillageHTMLTooltipGenerator {

    public static String buildToolTip(Village pVillage) {
        return buildToolTip(pVillage, true);
    }

    public static String buildToolTip(Village pVillage, boolean pWithUnits) {
        boolean showMoral = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.moral"));
        boolean showRanks = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.ranks"));
        boolean showConquers = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.conquers"));
        boolean showFarmSpace = Boolean.parseBoolean(GlobalOptions.getProperty("show.popup.farm.space"));
        String res = "<html><table width=\"400\" style=\"border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;\">\n";
        res += buildVillageRow(pVillage);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        res += buildInfoRow("Punkte:", nf.format(pVillage.getPoints()));
        if (pVillage.getTribe() != null) {
            res += buildInfoRow("Besitzer:", pVillage.getTribe());
            if (showRanks) {
                res += buildSubInfoRow("Punkte:", nf.format(pVillage.getTribe().getPoints()) + " (" + nf.format(pVillage.getTribe().getRank()) + ")");
            }
            if (showConquers) {
                res += buildSubInfoRow("Besiegte Gegner (Off):", nf.format(pVillage.getTribe().getKillsAtt()) + " (" + nf.format(pVillage.getTribe().getRankAtt()) + ")");
                res += buildSubInfoRow("Besiegte Gegner (Deff):", nf.format(pVillage.getTribe().getKillsDef()) + " (" + nf.format(pVillage.getTribe().getRankDef()) + ")");
            }
            if (pVillage.getTribe().getAlly() != null) {
                res += buildInfoRow("Stamm:", pVillage.getTribe().getAlly());
                if (showRanks) {
                    res += buildSubInfoRow("Punkte:", nf.format(pVillage.getTribe().getAlly().getPoints()) + " (" + nf.format(pVillage.getTribe().getAlly().getRank()) + ")");
                    res += buildSubInfoRow("Member:", nf.format(pVillage.getTribe().getAlly().getMembers()));
                }
            }
            if (showMoral) {
                Tribe current = DSWorkbenchMainFrame.getSingleton().getCurrentUser();
                if (current != null) {
                    if (!current.equals(pVillage.getTribe())) {
                        double moral = ((pVillage.getTribe().getPoints() / current.getPoints()) * 3 + 0.3) * 100;
                        moral = (moral > 100) ? 100 : moral;
                        res += buildInfoRow("Moral:", nf.format(moral) + "%");
                    }
                }
            }
        } else {
            if (showMoral) {
                res += buildInfoRow("Moral:", "100%");
            }
        }

        List<Tag> tags = TagManager.getSingleton().getTags(pVillage);
        if (tags != null && !tags.isEmpty()) {
            String tagString = "";
            for (Tag t : tags) {
                tagString += t.getName() + ";";
            }
            tagString = tagString.substring(0, tagString.lastIndexOf(";"));
            res += buildInfoRow("Tags:", tagString);
        }
        if (showFarmSpace) {
            res += buildFarmLevel(pVillage);
        }
        Conquer c = ConquerManager.getSingleton().getConquer(pVillage);
        if (c != null) {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            res += buildInfoRow("Erobert am:", f.format((long) c.getTimestamp() * 1000));
            res += buildInfoRow("Zustimmung:", c.getCurrentAcceptance());
        }
        if (pWithUnits) {
            res += buildUnitTableRow(pVillage);
        }
        res += "</table>\n";
        res += "</html>\n";
        return res;
    }

    static String buildVillageRow(Village pVillage) {
        String res = "<tr>\n";
        res += "<td colspan='3' bgcolor='#E1D5BE'><strong>" + pVillage.getFullName() + "</strong></td>\n";
        res += "</tr>\n";
        return res;
    }

    static String buildInfoRow(String pField, Object pValue) {
        String res = "<tr>\n";
        res += "<td width='150'><strong>" + pField + "</strong></td>\n";
        if (pValue instanceof Tribe) {
            Tribe t = (Tribe) pValue;
            Marker m = MarkerManager.getSingleton().getMarker(t);
            if (m != null) {
                String rgb = Integer.toHexString(m.getMarkerColor().getRGB());
                res += "<td width='300'>" + pValue + "</td>\n";
                res += "<td width='5' bgcolor='#" + rgb.substring(2) + "'>&nbsp;</td>\n";
            } else {
                res += "<td width='300'>" + pValue + "</td>\n";
            }
        } else if (pValue instanceof Ally) {
            Ally a = (Ally) pValue;
            Marker m = MarkerManager.getSingleton().getMarker(a);
            if (m != null) {
                String rgb = Integer.toHexString(m.getMarkerColor().getRGB());
                res += "<td width='300'>" + a + "</td>\n";
                res += "<td width='5' bgcolor='#" + rgb.substring(2) + "'>&nbsp;</td>\n";
            } else {
                res += "<td width='300'>" + pValue + "</td>\n";
            }
        } else {
            res += "<td colspan='2' width='300'>" + pValue + "</td>\n";
        }

        res += "</tr>\n";

        return res;
    }

    static String buildSubInfoRow(String pField, Object pValue) {
        String res = "<tr>\n";
        res += "<td width=\"150\" style=\"font-size:8px;\">&nbsp;&nbsp;&nbsp;" + pField + "</td>\n";
        res += "<td colspan='2' width=\"300\" >" + pValue + "</td>\n";
        res += "</tr>\n";
        return res;
    }

    static String buildFarmLevel(Village pVillage) {
        String res = "<tr>\n";
        res += "<td width=\"150\"><strong>Bauernhof:</strong></td>\n";
        res += "<td colspan='2' width=\"300\">\n";
        res += "<table width='100%' style=\"font-size:8px;border: 0px black; padding: 0px;\">\n";

        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage);
        if (holder != null) {
            float farmSpace = holder.getFarmSpace() * 100.f;
            URL red = VillageHTMLTooltipGenerator.class.getResource("/res/balken_pech.png");
            URL green = VillageHTMLTooltipGenerator.class.getResource("/res/balken_glueck.png");
            if (farmSpace == 100) {
                res += "<tr>\n";
                res += "<td width='100%' style='background:url(" + green + ");background-repeat:repeat-x;background-position:bottom;'>&nbsp;</td>\n";
                res += "</tr>\n";
            } else {
                res += "<tr>\n";
                res += "<td style='background:url(" + green + ");background-repeat:repeat-x;background-position:bottom;' width=\"" + farmSpace + "%\">&nbsp;</td>\n";
                res += "<td style='background:url(" + red + ");background-repeat:repeat-x;background-position:bottom;' width=\"" + (100 - farmSpace) + "%\">&nbsp;</td>\n";
                res += "</tr>\n";
            }
        } else {
            res += "<tr>\n";
            res += "<td width='100%'>Keine Informationen</td>\n";
            res += "</tr>\n";
        }

        res += "</table>\n";
        res += "</td>\n";
        res += "</tr>\n";
        return res;

    }

    static String buildUnitTableRow(Village pVillage) {
        String res = "<tr>\n";
        res += "<td colspan=\"3\">\n";
        res += "<table width=\"100%\" style=\"border: solid 1px black; padding: 4px;background-color:#EFEBDF;\">\n";
        res += "<tr>\n";
        //add unit table
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage);
        Village current = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
        if (holder == null && (current != null && current.equals(pVillage))) {
            return "";
        }

        int cnt = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (cnt % 2 == 0) {
                res += "<td style=\"background-color:#FFFFFF;font-size:95%;font-family:Verdana\"><div align=\"center\">";
            } else {
                res += "<td style=\"background-color:#E1D5BE;font-size:95%;font-family:Verdana\"><div align=\"center\">";
            }
            res += "<img src=\"" + VillageHTMLTooltipGenerator.class.getResource("/res/ui/" + unit.getPlainName() + ".png") + "\"/>";
            res += "<BR/>\n";
            if (holder != null) {
                Integer amount = holder.getTroopsInVillage().get(unit);
                if (amount == 0) {
                    res += "<font style=\"color:#DED3B9;\">0</font>\n";
                } else {
                    res += "<font>" + amount + "</font>\n";
                }
                res += "<BR/>\n";
                amount = holder.getTroopsOutside().get(unit);
                if (amount == 0) {
                    res += "<font style=\"color:#DED3B9;\">0</font>\n";
                } else {
                    res += "<font>" + amount + "</font>\n";
                }
                res += "<BR/>\n";
                amount = holder.getTroopsOnTheWay().get(unit);
                if (amount == 0) {
                    res += "<font style=\"color:#DED3B9;\">0</font>\n";
                } else {
                    res += "<font>" + amount + "</font>\n";
                }
                res += "<BR/>\n";
            }
            if (current != null && !current.equals(pVillage)) {
                double runtime = DSCalculator.calculateMoveTimeInMinutes(current, pVillage, unit.getSpeed());
                res += "<i>" + DSCalculator.formatTimeInMinutes(runtime) + "</i>";
            }
            res += "</div>";
            res += "</td>";

            cnt++;
        }
        res += "</tr>\n";
        res += "</table>\n";
        res += "</td>\n";
        res += "</tr>\n";
        return res;
    }
}
