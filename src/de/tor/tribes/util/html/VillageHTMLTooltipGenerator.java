/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.html;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.Marker;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.ui.ImageManager;
import de.tor.tribes.ui.MapPanel;
import de.tor.tribes.util.BBCodeFormatter;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.mark.MarkerManager;
import de.tor.tribes.util.note.NoteManager;
import de.tor.tribes.util.tag.TagManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        String res = "<html><head>" + BBCodeFormatter.getStyles() + "</head><table width=\"400\" style=\"border: solid 1px black; cellspacing:0px;cellpadding: 0px;background-color:#EFEBDF;\">\n";
        res += buildVillageRow(pVillage);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        res += buildInfoRow("Punkte:", nf.format(pVillage.getPoints()), false);
        if (pVillage.getTribe() != Barbarians.getSingleton()) {
            res += buildInfoRow("Besitzer:", pVillage.getTribe(), showRanks);
            if (showConquers) {
                res += buildSubInfoRow("Besiegte Gegner (Off):", nf.format(pVillage.getTribe().getKillsAtt()) + " (" + nf.format(pVillage.getTribe().getRankAtt()) + ". Platz)");
                res += buildSubInfoRow("Besiegte Gegner (Deff):", nf.format(pVillage.getTribe().getKillsDef()) + " (" + nf.format(pVillage.getTribe().getRankDef()) + ". Platz)");
            }

            if (pVillage.getTribe().getAlly() != null) {
                res += buildInfoRow("Stamm:", pVillage.getTribe().getAlly(), showRanks);
            }
            if (showMoral) {
                Tribe current = GlobalOptions.getSelectedProfile().getTribe();
                if (current != null) {
                    if (!current.equals(pVillage.getTribe())) {
                        double moral = ((pVillage.getTribe().getPoints() / current.getPoints()) * 3 + 0.3) * 100;
                        moral = (moral > 100) ? 100 : moral;
                        res += buildInfoRow("Moral:", nf.format(moral) + "%", false);
                    }
                }
            }
        } else {
            if (showMoral) {
                res += buildInfoRow("Moral:", "100%", false);
            }
        }

        List<Tag> tags = TagManager.getSingleton().getTags(pVillage);
        if (tags != null && !tags.isEmpty()) {
            String tagString = "";
            for (Tag t : tags) {
                tagString += t.getName() + ";";
            }
            tagString = tagString.substring(0, tagString.lastIndexOf(";"));
            res += buildInfoRow("Tags:", tagString, false);
        }
        if (showFarmSpace) {
            res += buildFarmLevel(pVillage);
        }
        Conquer c = ConquerManager.getSingleton().getConquer(pVillage);
        if (c != null) {
            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            res += buildInfoRow("Erobert am:", f.format(c.getTimestamp() * 1000l), false);
            res += buildInfoRow("Zustimmung:", c.getCurrentAcceptance(), false);
        }

        res += buildNotes(pVillage);

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

    static String buildInfoRow(String pField, Object pValue, boolean pExtended) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String res = "<tr>\n";
        res += "<td width='150'><strong>" + pField + "</strong></td>\n";
        if (pValue instanceof Tribe) {
            Tribe t = (Tribe) pValue;
            Marker m = MarkerManager.getSingleton().getMarker(t);
            if (m != null) {
                String rgb = Integer.toHexString(m.getMarkerColor().getRGB());
                if (pExtended) {
                    String tribeText = t.getName();
                    tribeText += " <i>(" + nf.format(t.getPoints()) + " Punkte, " + nf.format(t.getRank()) + ". Platz)</i>";
                    res += "<td width='300'>" + tribeText + "</td>\n";
                } else {
                    res += "<td width='300'>" + t + "</td>\n";
                }
                res += "<td width='5' bgcolor='#" + rgb.substring(2) + "'>&nbsp;</td>\n";
            } else {
                if (pExtended) {
                    String tribeText = t.getName();
                    tribeText += " <i>(" + nf.format(t.getPoints()) + " Punkte, " + nf.format(t.getRank()) + ". Platz)</i>";
                    res += "<td width='300'>" + tribeText + "</td>\n";
                } else {
                    res += "<td width='300'>" + t + "</td>\n";
                }
            }
        } else if (pValue instanceof Ally) {
            Ally a = (Ally) pValue;
            Marker m = MarkerManager.getSingleton().getMarker(a);
            if (m != null) {
                String rgb = Integer.toHexString(m.getMarkerColor().getRGB());
                if (pExtended) {
                    String allyText = a.getName() + " <i><b>" + a.getTag() + "</b></i>";
                    allyText += " <i>(" + nf.format(a.getPoints()) + " Punkte, " + nf.format(a.getRank()) + ". Platz)</i>";
                    res += "<td width='300'>" + allyText + "</td>\n";
                } else {
                    res += "<td width='300'>" + a.getName() + " <i><b>" + a.getTag() + "</b></i>" + "</td>\n";
                }
                res += "<td width='5' bgcolor='#" + rgb.substring(2) + "'>&nbsp;</td>\n";
            } else {
                if (pExtended) {
                    String allyText = a.getName() + " <i><b>" + a.getTag() + "</b></i>";
                    allyText += " <i>(" + nf.format(a.getPoints()) + " Punkte, " + nf.format(a.getRank()) + ". Platz)</i>";
                    res += "<td width='300'>" + allyText + "</td>\n";
                } else {
                    res += "<td width='300'>" + a.getName() + " <i><b>" + a.getTag() + "</b></i>" + "</td>\n";
                }
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
        VillageTroopsHolder inVillage = TroopsManager.getSingleton().getTroopsForVillage(pVillage);
        VillageTroopsHolder outside = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.OUTWARDS);
        VillageTroopsHolder onTheWay = TroopsManager.getSingleton().getTroopsForVillage(pVillage, TroopsManager.TROOP_TYPE.ON_THE_WAY);

        Village current = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
        /* if (inVillage == null && outside == null && onTheWay == null) {//&& (current != null && current.equals(pVillage))) {
        //we have the active user village but no troops
        //            return "";
        outside = new VillageTroopsHolder(current, new Date());
        inVillage = new VillageTroopsHolder(current, new Date());
        onTheWay = new VillageTroopsHolder(current, new Date());
        
        }*/

        int cnt = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            if (cnt % 2 == 0) {
                res += "<td style=\"background-color:#FFFFFF;font-size:95%;font-family:Verdana\"><div align=\"center\">";
            } else {
                res += "<td style=\"background-color:#E1D5BE;font-size:95%;font-family:Verdana\"><div align=\"center\">";
            }
            res += "<img src=\"" + VillageHTMLTooltipGenerator.class.getResource("/res/ui/" + unit.getPlainName() + ".png") + "\"/>";
            res += "<BR/>\n";
            if (inVillage != null) {
                Integer amount = inVillage.getTroopsOfUnitInVillage(unit);
                if (amount == 0) {
                    res += "<font style=\"color:#DED3B9;\">0</font>\n";
                } else {
                    res += "<font>" + amount + "</font>\n";
                }
                res += "<BR/>\n";
                amount = (outside == null) ? 0 : outside.getTroopsOfUnitInVillage(unit);
                if (amount == 0) {
                    res += "<font style=\"color:#DED3B9;\">0</font>\n";
                } else {
                    res += "<font>" + amount + "</font>\n";
                }
                res += "<BR/>\n";
                amount = (onTheWay == null) ? 0 : onTheWay.getTroopsOfUnitInVillage(unit);
                if (amount == 0) {
                    res += "<font style=\"color:#DED3B9;\">0</font>\n";
                } else {
                    res += "<font>" + amount + "</font>\n";
                }
                res += "<BR/>\n";
            }
            Village toolSource = MapPanel.getSingleton().getToolSourceVillage();
            if (toolSource == null) {
                if (current != null && !current.equals(pVillage)) {
                    double runtime = DSCalculator.calculateMoveTimeInMinutes(current, pVillage, unit.getSpeed());
                    res += "<i>" + DSCalculator.formatTimeInMinutes(runtime) + "</i>";
                }
            } else {
                //tool source village is not null
                double runtime = DSCalculator.calculateMoveTimeInMinutes(toolSource, pVillage, unit.getSpeed());
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

    static String buildNotes(Village pVillage) {
        List<Note> notes = NoteManager.getSingleton().getNotesForVillage(pVillage);
        String lines = "";
        for (Note n : notes) {
            //Note n = NoteManager.getSingleton().getNoteForVillage(pVillage);
            if (n == null) {
                return "";
            }
            try {
                String res = "<tr>\n";
                String text = n.getNoteText();
                if (text == null) {
                    text = "";
                }
                text = text.replaceAll("\n", "<br/>");
                if (n.getNoteSymbol() == -1) {
                    res += "<td colspan='3' bgcolor='#F7F5BF'>" + BBCodeFormatter.toHtml(text) + "</td>\n";
                } else {
                    res += "<td bgcolor='#F7F5BF'>" + "<img src=\"" + ImageManager.getNoteImageURL(n.getNoteSymbol()) + "\"/>" + "</td>\n";
                    res += "<td colspan='2' bgcolor='#F7F5BF'>" + BBCodeFormatter.toHtml(text) + "</td>\n";
                }
                res += "</tr>\n";
                // return res;
                lines += res;
            } catch (Exception e) {
                return "";
            }
        }
        return lines;
    }
}
