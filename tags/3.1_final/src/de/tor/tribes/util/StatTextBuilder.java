/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.types.TribeStatsElement.Stats;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
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
                double perc = 0;
                if (pBefore > 0) {
                    perc = (double) 100 * (double) diff / (double) pBefore;
                }

                if (pUseBBCodes) {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%)\n";
                } else {
                    result += " (" + ((perc >= 0) ? "+" : "") + nf.format(perc) + "%) ";
                }
            }

            double killsPerPonts = s.getKillPerPoint();
            if (pUseBBCodes) {
                result += "[color=" + ((killsPerPonts > 0) ? "green]" : "red]") + nf.format(killsPerPonts) + " Kills/Punkt[/color]\n";
            } else {
                result += nf.format(killsPerPonts) + " Kills/Punkt\n";
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
                double perc = 0;
                if (pBefore > 0) {
                    perc = (double) 100 * (double) diff / (double) pBefore;
                }
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
                double perc = 0;
                if (pBefore > 0) {
                    perc = (double) 100 * (double) diff / (double) pBefore;
                }
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

    public static String buildWinnerStats(List<Stats> pStats, boolean pUseBBCodes, boolean pShowPercent, boolean pUseTop10Only) {
        String result = "";
        Collections.sort(pStats, Stats.POINTS_COMPARATOR);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        //get relative values
        long absPoints = -1;
        List<Stats> absPointsList = new LinkedList<Stats>();
        double relPoints = 0.0;
        List<Stats> relPointsList = new LinkedList<Stats>();
        double relVillages = 0.0;
        List<Stats> relVillageList = new LinkedList<Stats>();
        double relBashOff = 0.0;
        List<Stats> relBashOffList = new LinkedList<Stats>();
        double killsPerPoint = 0.0;
        List<Stats> killsPerPointList = new LinkedList<Stats>();
        double relBashDef = 0.0;
        List<Stats> relBashDefList = new LinkedList<Stats>();
        for (Stats elem : pStats) {
            // <editor-fold defaultstate="collapsed" desc="check abs points growing">
            if (absPoints == -1) {
                absPoints = elem.getPointDiff();
                absPointsList.add(elem);
            } else if (elem.getPointDiff() == absPoints) {
                absPointsList.add(elem);
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel points growing">
            double diffBest = elem.getPointDiff();
            double currPerc = 0;
            if (elem.getPointStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getPointStart();
            }

            if (currPerc > relPoints) {
                relPoints = currPerc;
                //remove all former elements and add only better elems
                relPointsList.clear();
                relPointsList.add(elem);
            } else if (currPerc == relPoints) {
                //add same value as we have
                relPointsList.add(elem);
            }

            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel village growing">
            diffBest = elem.getVillageDiff();
            currPerc = 0;
            if (elem.getVillageStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getVillageStart();
            }

            if (currPerc > relVillages) {
                relVillages = currPerc;
                //remove all former elements and add only better elems
                relVillageList.clear();
                relVillageList.add(elem);
            } else if (currPerc == relPoints) {
                //add same value as we have
                relVillageList.add(elem);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel bash off growing">
            diffBest = elem.getBashOffDiff();
            currPerc = 0;
            if (elem.getBashOffStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getBashOffStart();
            }

            if (currPerc > relBashOff) {
                relBashOff = currPerc;
                //remove all former elements and add only better elems
                relBashOffList.clear();
                relBashOffList.add(elem);
            } else if (currPerc == relBashOff) {
                //add same value as we have
                relBashOffList.add(elem);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check kills per point">
            diffBest = elem.getKillPerPoint();

            if (diffBest > killsPerPoint) {
                killsPerPoint = diffBest;
                //remove all former elements and add only better elems
                killsPerPointList.clear();
                killsPerPointList.add(elem);
            } else if (diffBest == killsPerPoint) {
                //add same value as we have
                killsPerPointList.add(elem);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel bash def growing">
            diffBest = elem.getBashDefDiff();
            currPerc = 0;
            if (elem.getBashDefStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getBashDefStart();
            }
            if (currPerc > relBashDef) {
                relBashDef = currPerc;
                //remove all former elements and add only better elems
                relBashDefList.clear();
                relBashDefList.add(elem);
            } else if (currPerc == relBashDef) {
                //add same value as we have
                relBashDefList.add(elem);
            }// </editor-fold>
        }

        // <editor-fold defaultstate="collapsed" desc="Add abs point diff">
        if (pUseBBCodes) {
            result += "[b]Punktesammler(in):[/b] ";
        } else {
            result += "Punktesammler(in): ";
        }

        for (Stats s : absPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        result = result.substring(0, result.lastIndexOf(","));

        if (pUseBBCodes) {
            result += " ([color=" + ((absPoints >= 0) ? "green]+" : "red]") + nf.format(absPoints) + " Punkte[/color])\n\n";
        } else {
            result += " (+" + nf.format(absPoints) + " Punkte)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel point diff">
        if (pUseBBCodes) {
            result += "[b]Überflieger(in):[/b] ";
        } else {
            result += "Überflieger(in): ";
        }
        nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relPoints >= 0) ? "green]" : "red]") + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "% Punktewachstum[/color])\n\n";
        } else {
            result += " (" + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "% Punktewachstum)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel village diff">
        if (pUseBBCodes) {
            result += "[b]Adelkönig(in):[/b] ";
        } else {
            result += "Adelkönig(in): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relVillageList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relVillages >= 0) ? "green]" : "red]") + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "% Dorfzuwachs[/color])\n\n";
        } else {
            result += " (" + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "% Dorfzuwachs)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add attacker diff (relative)">
        if (pUseBBCodes) {
            result += "[b]Unbeliebtester Spieler:[/b] ";
        } else {
            result += "Unbeliebtester Spieler: ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashOffList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashOff >= 0) ? "green]" : "red]") + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "% Zuwachs (Kills Off)[/color])\n\n";
        } else {
            result += " (" + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "% Zuwachs (Kills Off))\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add attacker diff (absolute)">
        Collections.sort(pStats, Stats.BASH_OFF_COMPARATOR);
        Stats best = pStats.get(0);
        if (pUseBBCodes) {
            result += "[b]'Angriff ist die beste Verteidigung':[/b] ";
        } else {
            result += "'Angriff ist die beste Verteidigung': ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        result += (pUseBBCodes) ? best.getParent().getTribe().toBBCode() : best.getParent().getTribe().toString();
        if (pUseBBCodes) {
            result += " ([color=" + ((best.getBashOffDiff() >= 0) ? "green]+" : "red]") + nf.format(best.getBashOffDiff()) + " Kills (Off)[/color])\n\n";
        } else {
            result += " (" + ((best.getBashOffDiff() >= 0) ? "+" : "") + nf.format(best.getBashOffDiff()) + " Kills (Off))\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add kills per point)">
        if (pUseBBCodes) {
            result += "[b]'Ein hart erarbeiteter Sieg':[/b] ";
        } else {
            result += "'Ein hart erarbeiteter Sieg': ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : killsPerPointList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((killsPerPoint >= 0) ? "green]" : "red]") + ((killsPerPoint >= 0) ? "+" : "") + nf.format(killsPerPoint) + " Kills pro Punkt[/color])\n\n";
        } else {
            result += " (" + ((killsPerPoint >= 0) ? "+" : "") + nf.format(killsPerPoint) + " Kills pro Punkt)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add defender diff">
        if (pUseBBCodes) {
            result += "[b]Beliebtester Spieler:[/b] ";
        } else {
            result += "Beliebtester Spieler: ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashDefList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashDef >= 0) ? "green]" : "red]") + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "% Zuwachs (Kills Deff)[/color])\n\n";
        } else {
            result += " (" + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "% Zuwachs (Kills Deff))\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add defender diff (absolute)">
        Collections.sort(pStats, Stats.BASH_DEF_COMPARATOR);
        best = pStats.get(0);
        if (pUseBBCodes) {
            result += "[b]'My Home is my Castle':[/b] ";
        } else {
            result += "'My Home is my Castle': ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        result += (pUseBBCodes) ? best.getParent().getTribe().toBBCode() : best.getParent().getTribe().toString();
        if (pUseBBCodes) {
            result += " ([color=" + ((best.getBashDefDiff() >= 0) ? "green]+" : "red]") + nf.format(best.getBashDefDiff()) + " Kills (Deff)[/color])\n\n";
        } else {
            result += " (" + ((best.getBashDefDiff() >= 0) ? "+" : "") + nf.format(best.getBashDefDiff()) + "% Kills (Deff))\n\n";
        }// </editor-fold>


        return result;
    }

    public static String buildLoserStats(List<Stats> pStats, boolean pUseBBCodes, boolean pShowPercent, boolean pUseTop10Only) {
        String result = "";
        Collections.sort(pStats, Stats.POINTS_COMPARATOR);
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        //get relative values
        long absPoints = -1;
        List<Stats> absPointsList = new LinkedList<Stats>();
        double relPoints = 0.0;
        List<Stats> relPointsList = new LinkedList<Stats>();
        double relVillages = 0.0;
        List<Stats> relVillageList = new LinkedList<Stats>();
        double relBashOff = 0.0;
        List<Stats> relBashOffList = new LinkedList<Stats>();
        double killsPerPoint = Double.MAX_VALUE;
        double killsPerPointVillages = 0;
        List<Stats> killsPerPointList = new LinkedList<Stats>();
        double relBashDef = 0.0;
        List<Stats> relBashDefList = new LinkedList<Stats>();
        for (Stats elem : pStats) {
            // <editor-fold defaultstate="collapsed" desc="check abs points growing">
            if (absPoints == -1) {
                absPoints = elem.getPointDiff();
                absPointsList.add(elem);
            } else if (elem.getPointDiff() < absPoints) {
                absPoints = elem.getPointDiff();
                absPointsList.clear();
                absPointsList.add(elem);
            } else if (elem.getPointDiff() == absPoints) {
                absPointsList.add(elem);
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel points growing">
            double diffBest = elem.getPointDiff();
            double currPerc = 0;
            if (elem.getPointStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getPointStart();
            }

            if (currPerc < relPoints) {
                relPoints = currPerc;
                //remove all former elements and add only better elems
                relPointsList.clear();
                relPointsList.add(elem);
            } else if (currPerc == relPoints) {
                //add same value as we have
                relPointsList.add(elem);
            }

            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel village growing">
            diffBest = elem.getVillageDiff();
            currPerc = 0;
            if (elem.getVillageStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getVillageStart();
            }

            if (currPerc < relVillages) {
                relVillages = currPerc;
                //remove all former elements and add only better elems
                relVillageList.clear();
                relVillageList.add(elem);
            } else if (currPerc == relPoints) {
                //add same value as we have
                relVillageList.add(elem);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel bash off growing">
            diffBest = elem.getBashOffDiff();
            currPerc = 0;
            if (elem.getBashOffStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getBashOffStart();
            }

            if (currPerc < relBashOff) {
                relBashOff = currPerc;
                //remove all former elements and add only better elems
                relBashOffList.clear();
                relBashOffList.add(elem);
            } else if (currPerc == relBashOff) {
                //add same value as we have
                relBashOffList.add(elem);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check kills per point">
            diffBest = elem.getKillPerPoint();
            int diffVillages = elem.getVillageDiff();
            if (diffBest < killsPerPoint && diffVillages > 0) {
                killsPerPoint = diffBest;
                killsPerPointVillages = diffVillages;
                //remove all former elements and add only better elems
                killsPerPointList.clear();
                killsPerPointList.add(elem);
            } else if (diffBest == killsPerPoint && elem.getVillageDiff() > 0) {
                //check if tribe has conquered more villages
                if (killsPerPointVillages == diffVillages) {
                    //same bash count, same village amount -> add element
                    killsPerPointList.add(elem);
                } else if (diffVillages > killsPerPointVillages) {
                    //same bash count, more village -> new winner
                    killsPerPoint = diffBest;
                    killsPerPointVillages = diffVillages;
                    //remove all former elements and add only better elems
                    killsPerPointList.clear();
                    killsPerPointList.add(elem);
                }
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="check rel bash def growing">
            diffBest = elem.getBashDefDiff();
            currPerc = 0;
            if (elem.getBashDefStart() != 0) {
                currPerc = (double) 100 * (double) diffBest / (double) elem.getBashDefStart();
            }
            if (currPerc < relBashDef) {
                relBashDef = currPerc;
                //remove all former elements and add only better elems
                relBashDefList.clear();
                relBashDefList.add(elem);
            } else if (currPerc == relBashDef) {
                //add same value as we have
                relBashDefList.add(elem);
            }// </editor-fold>
        }

        // <editor-fold defaultstate="collapsed" desc="Add abs point diff">
        if (pUseBBCodes) {
            result += "[b]Punktespender(in):[/b] ";
        } else {
            result += "Punktespender(in): ";
        }

        for (Stats s : absPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        result = result.substring(0, result.lastIndexOf(","));

        if (pUseBBCodes) {
            result += " ([color=" + ((absPoints >= 0) ? "green]" : "red]") + nf.format(absPoints) + " Punkte[/color])\n\n";
        } else {
            result += " (" + nf.format(absPoints) + " Punkte\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel point diff">
        if (pUseBBCodes) {
            result += "[b]Sorgenkind:[/b] ";
        } else {
            result += "Sorgenkind: ";
        }
        nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        int idx = result.lastIndexOf(",");
        if (idx > 0) {
            result = result.substring(0, idx);
        }

        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relPoints >= 0) ? "green]" : "red]") + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "% Punkteverlust[/color])\n\n";
        } else {
            result += " (" + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "% Punkteverlust)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel village diff">
        if (pUseBBCodes) {
            result += "[b]Dorfspender(in):[/b] ";
        } else {
            result += "Dorfspender(in): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relVillageList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relVillages >= 0) ? "green]" : "red]") + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "% Dorfverlust[/color])\n\n";
        } else {
            result += " (" + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "% Dorfverlust)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add kills per point diff">
        if (pUseBBCodes) {
            result += "[b]Aufadelkönig(in):[/b] ";
        } else {
            result += "Aufadelkönig(in): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : killsPerPointList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((killsPerPoint > 0) ? "green]" : "red]") + ((killsPerPoint > 0) ? "+" : "") + nf.format(killsPerPoint) + " Kills pro Punkt[/color])\n\n";
        } else {
            result += " (" + ((killsPerPoint > 0) ? "+" : "") + nf.format(killsPerPoint) + " Kills pro Punkt)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add attacker diff">
        if (pUseBBCodes) {
            result += "[b]Friedensaktivisten:[/b] ";
        } else {
            result += "Friedensaktivisten: ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashOffList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashOff >= 0) ? "green]" : "red]") + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "% Zuwachs (Kills Off)[/color])\n\n";
        } else {
            result += " (" + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "% Zuwachs (Kills Off))\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add defender diff">
        if (pUseBBCodes) {
            result += "[b]Lieblinge des Feindes:[/b] ";
        } else {
            result += "Lieblinge des Feindes: ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashDefList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashDef >= 0) ? "green]" : "red]") + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "% Zuwachs Kills (Deff)[/color])\n\n";
        } else {
            result += " (" + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "% Zuwachs Kills (Deff))\n\n";
        }// </editor-fold>

        return result;
    }
}
