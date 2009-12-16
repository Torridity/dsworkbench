/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.TribeStatsElement.Stats;
import de.tor.tribes.ui.MapPanel;
import java.awt.FontMetrics;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class StatTextBuilder {

    public static String buildPointsList(List<Stats> pStats, boolean pUseBBCodes, boolean pShowPercent, boolean pTop10Only) {
        String result = "";

        Collections.sort(pStats, Stats.POINTS_COMPARATOR);
        int cnt = 1;

        for (Stats s : pStats) {
            if (cnt < 10) {
                result += "0" + cnt + ". ";
            } else {
                result += cnt + ". ";
            }
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + "\n") : (s.getParent().getTribe().toString() + " ");
            if (pUseBBCodes) {
                result += "[quote]" + nf.format(s.getPointStart()) + " (Vorher)\n";
            } else {
                result += nf.format(s.getPointStart()) + " (Vorher) ";
            }
            long diff = s.getPointDiff();
            if (pUseBBCodes) {
                result += "[color=" + ((diff > 0) ? "green]" : "red]") + nf.format(diff) + "[/color]";
            } else {
                result += nf.format(diff);
            }
            nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            if (pShowPercent) {
                long pBefore = s.getPointStart();
                double perc = (double) 100 * (double) diff / (double) pBefore;
                if (pUseBBCodes) {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%)\n";
                } else {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%) ";
                }
            }

            nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            int villageDiff = s.getVillageDiff();
            if (villageDiff != 0) {
                if (pUseBBCodes) {
                    result += "[color=" + ((villageDiff > 0) ? "green]+" : "red]") + nf.format(villageDiff) + " ";
                    result += (Math.abs(villageDiff) == 1) ? "Dorf[/color]\n" : "Dörfer[/color]\n";
                } else {
                    result += ((villageDiff > 0) ? "+" : "-") + nf.format(villageDiff) + " (Dörfer) ";
                }
            }

            if (pUseBBCodes) {
                result += nf.format(s.getPointEnd()) + " (Nachher)[/quote]\n";
            } else {
                result += nf.format(s.getPointEnd()) + " (Nachher)\n";
            }
            cnt++;
            if (pTop10Only && cnt > 10) {
                break;
            }
        }
        return result;
    }

    public static String buildBashOffList(List<Stats> pStats, boolean pUseBBCodes, boolean pShowPercent, boolean pTop10Only) {
        String result = "";

        Collections.sort(pStats, Stats.BASH_OFF_COMPARATOR);
        int cnt = 1;

        for (Stats s : pStats) {
            if (cnt < 10) {
                result += "0" + cnt + ". ";
            } else {
                result += cnt + ". ";
            }
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + "\n") : (s.getParent().getTribe().toString() + " ");
            if (pUseBBCodes) {
                result += "[quote]" + nf.format(s.getBashOffStart()) + " (Vorher)\n";
            } else {
                result += nf.format(s.getBashOffStart()) + " (Vorher) ";
            }
            long diff = s.getBashOffDiff();
            if (pUseBBCodes) {
                result += "[color=" + ((diff > 0) ? "green]" : "red]") + nf.format(diff) + "[/color]";
            } else {
                result += nf.format(diff);
            }
            nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            if (pShowPercent) {
                long pBefore = s.getBashOffStart();
                double perc = (double) 100 * (double) diff / (double) pBefore;
                if (pUseBBCodes) {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%)\n";
                } else {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%) ";
                }
            }

            nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            int rankOffDiff = s.getRankOffDiff();
            if (rankOffDiff != 0) {
                if (pUseBBCodes) {
                    result += "[color=" + ((rankOffDiff > 0) ? "red]-" : "green]+") + nf.format(Math.abs(rankOffDiff)) + " ";
                    result += (Math.abs(rankOffDiff) == 1) ? "Platz in der Rangliste[/color]\n" : "Plätze in der Rangliste[/color]\n";
                } else {
                    result += ((rankOffDiff > 0) ? "-" : "+") + nf.format(Math.abs(rankOffDiff)) + " (BashOff Rang) ";
                }
            }

            if (pUseBBCodes) {
                result += nf.format(s.getBashOffEnd()) + " (Nachher)[/quote]\n";
            } else {
                result += nf.format(s.getBashOffEnd()) + " (Nachher)\n";
            }
            cnt++;
            if (pTop10Only && cnt > 10) {
                break;
            }
        }
        return result;
    }

    public static String buildBashDefList(List<Stats> pStats, boolean pUseBBCodes, boolean pShowPercent, boolean pTop10Only) {
        String result = "";

        Collections.sort(pStats, Stats.BASH_DEF_COMPARATOR);
        int cnt = 1;

        for (Stats s : pStats) {
            if (cnt < 10) {
                result += "0" + cnt + ". ";
            } else {
                result += cnt + ". ";
            }
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + "\n") : (s.getParent().getTribe().toString() + " ");
            if (pUseBBCodes) {
                result += "[quote]" + nf.format(s.getBashDefStart()) + " (Vorher)\n";
            } else {
                result += nf.format(s.getBashDefStart()) + " (Vorher) ";
            }
            long diff = s.getBashDefDiff();
            if (pUseBBCodes) {
                result += "[color=" + ((diff > 0) ? "green]" : "red]") + nf.format(diff) + "[/color]";
            } else {
                result += nf.format(diff);
            }
            nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            if (pShowPercent) {
                long pBefore = s.getBashDefStart();
                double perc = (double) 100 * (double) diff / (double) pBefore;
                if (pUseBBCodes) {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%)\n";
                } else {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%) ";
                }
            }

            nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            int rankDefDiff = s.getRankDefDiff();
            if (rankDefDiff != 0) {
                if (pUseBBCodes) {
                    result += "[color=" + ((rankDefDiff > 0) ? "red]-" : "green]+") + nf.format(Math.abs(rankDefDiff)) + " ";
                    result += (Math.abs(rankDefDiff) == 1) ? "Platz in der Rangliste[/color]\n" : "Plätze in der Rangliste[/color]\n";
                } else {
                    result += ((rankDefDiff > 0) ? "-" : "+") + nf.format(Math.abs(rankDefDiff)) + " (BashDef Rang) ";
                }
            }

            if (pUseBBCodes) {
                result += nf.format(s.getBashDefEnd()) + " (Nachher)[/quote]\n";
            } else {
                result += nf.format(s.getBashDefEnd()) + " (Nachher)\n";
            }
            cnt++;
            if (pTop10Only && cnt > 10) {
                break;
            }
        }
        return result;
    }
}
