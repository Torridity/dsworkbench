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
            result += "[b]Größtes Punktwachstum (Absolut):[/b] ";
        } else {
            result += "Größtes Punktewachstum (Absolut): ";
        }

        for (Stats s : absPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        result = result.substring(0, result.lastIndexOf(","));

        if (pUseBBCodes) {
            result += " ([color=" + ((absPoints >= 0) ? "green]+" : "red]") + nf.format(absPoints) + "[/color])\n\n";
        } else {
            result += " (" + nf.format(absPoints) + "\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel point diff">
        if (pUseBBCodes) {
            result += "[b]Größtes Punktwachstum (Relativ):[/b] ";
        } else {
            result += "Größtes Punktewachstum (Relativ): ";
        }
        nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (+" + nf.format(s.getPointDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getPointDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relPoints >= 0) ? "green]" : "red]") + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "%[/color])\n\n";
        } else {
            result += " (" + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel village diff">
        if (pUseBBCodes) {
            result += "[b]Größter Dorfgewinn:[/b] ";
        } else {
            result += "Größtes Dorfgewinn: ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relVillageList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (+" + nf.format(s.getVillageDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getVillageDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relVillages >= 0) ? "green]" : "red]") + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "%[/color])\n\n";
        } else {
            result += " (" + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add attacker diff (relative)">
        if (pUseBBCodes) {
            result += "[b]Bester Angreifer (Relativ):[/b] ";
        } else {
            result += "Bester Angreifer (Relativ): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashOffList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (+" + nf.format(s.getBashOffDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getBashOffDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashOff >= 0) ? "green]" : "red]") + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "%[/color])\n\n";
        } else {
            result += " (" + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add attacker diff (absolute)">
        Collections.sort(pStats, Stats.BASH_OFF_COMPARATOR);
        Stats best = pStats.get(0);
        if (pUseBBCodes) {
            result += "[b]Bester Angreifer (Absolut):[/b] ";
        } else {
            result += "Bester Angreifer (Absolut): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        result += (pUseBBCodes) ? best.getParent().getTribe().toBBCode() : best.getParent().getTribe().toString();
        if (pUseBBCodes) {
            result += " ([color=" + ((best.getBashOffDiff() >= 0) ? "green]+" : "red]") + nf.format(best.getBashOffDiff()) + "[/color])\n\n";
        } else {
            result += " (" + ((best.getBashOffDiff() >= 0) ? "+" : "") + nf.format(best.getBashOffDiff()) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add defender diff">
        if (pUseBBCodes) {
            result += "[b]Bester Verteidiger (Relativ):[/b] ";
        } else {
            result += "Bester Verteidiger (Relativ): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashDefList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (+" + nf.format(s.getBashDefDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getBashDefDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashDef >= 0) ? "green]" : "red]") + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "%[/color])\n\n";
        } else {
            result += " (" + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add defender diff (absolute)">
        Collections.sort(pStats, Stats.BASH_DEF_COMPARATOR);
        best = pStats.get(0);
        if (pUseBBCodes) {
            result += "[b]Bester Verteidiger (Absolut):[/b] ";
        } else {
            result += "Bester Verteidiger (Absolut): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        result += (pUseBBCodes) ? best.getParent().getTribe().toBBCode() : best.getParent().getTribe().toString();
        if (pUseBBCodes) {
            result += " ([color=" + ((best.getBashDefDiff() >= 0) ? "green]+" : "red]") + nf.format(best.getBashDefDiff()) + "[/color])\n\n";
        } else {
            result += " (" + ((best.getBashDefDiff() >= 0) ? "+" : "") + nf.format(best.getBashDefDiff()) + "%)\n\n";
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
            result += "[b]Größter Punktverlust(Absolut):[/b] ";
        } else {
            result += "Größtes Punktverlust (Absolut): ";
        }

        for (Stats s : absPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + ", ") : (s.getParent().getTribe().toString() + ", ");
        }
        result = result.substring(0, result.lastIndexOf(","));

        if (pUseBBCodes) {
            result += " ([color=" + ((absPoints >= 0) ? "green]" : "red]") + nf.format(absPoints) + "[/color])\n\n";
        } else {
            result += " (" + nf.format(absPoints) + "\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel point diff">
        if (pUseBBCodes) {
            result += "[b]Größtes Punktverlust(Relativ):[/b] ";
        } else {
            result += "Größtes Punktverlust (Relativ): ";
        }
        nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relPointsList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (" + nf.format(s.getPointDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getPointDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relPoints >= 0) ? "green]" : "red]") + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "%[/color])\n\n";
        } else {
            result += " (" + ((relPoints >= 0) ? "+" : "") + nf.format(relPoints) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add rel village diff">
        if (pUseBBCodes) {
            result += "[b]Größter Dorfverlust:[/b] ";
        } else {
            result += "Größtes Dorfverlust: ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relVillageList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (" + nf.format(s.getVillageDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getVillageDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relVillages >= 0) ? "green]" : "red]") + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "%[/color])\n\n";
        } else {
            result += " (" + ((relVillages >= 0) ? "+" : "") + nf.format(relVillages) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add attacker diff">
        if (pUseBBCodes) {
            result += "[b]Inaktivster Angreifer (Relativ):[/b] ";
        } else {
            result += "Inaktivster Angreifer (Relativ): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashOffList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (+" + nf.format(s.getBashOffDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getBashOffDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashOff >= 0) ? "green]" : "red]") + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "%[/color])\n\n";
        } else {
            result += " (" + ((relBashOff >= 0) ? "+" : "") + nf.format(relBashOff) + "%)\n\n";
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Add defender diff">
        if (pUseBBCodes) {
            result += "[b]Inaktivster Verteidiger (Relativ):[/b] ";
        } else {
            result += "Inaktivster Verteidiger (Relativ): ";
        }

        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        for (Stats s : relBashDefList) {
            result += (pUseBBCodes) ? (s.getParent().getTribe().toBBCode() + " (+" + nf.format(s.getBashDefDiff()) + "), ") : (s.getParent().getTribe().toString() + " (+" + nf.format(s.getBashDefDiff()) + "), ");
        }
        //remove last comma
        result = result.substring(0, result.lastIndexOf(","));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (pUseBBCodes) {
            result += " ([color=" + ((relBashDef >= 0) ? "green]" : "red]") + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "%[/color])\n\n";
        } else {
            result += " (" + ((relBashDef >= 0) ? "+" : "") + nf.format(relBashDef) + "%)\n\n";
        }// </editor-fold>

        return result;
    }
}
