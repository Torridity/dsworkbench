/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.models.TroopsTableModel;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Jejkal
 */
public class TroopInformationToBBCodeFormater {

    public static String formatTroopInformation(Village pVillage, int pType, boolean pFully, String pServerURL, boolean pExtended) {
       //@TODO implement list formatter
      /*  VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pVillage);
        String result = "[u]Truppeninformationen für " + pVillage.toBBCode() + "[/u]\n\n";

        if (holder != null) {
            if (!pFully) {
                Hashtable<UnitHolder, Integer> activeList = null;
                switch (pType) {
                    case TroopsManagerTableModel.SHOW_OWN_TROOPS:
                        result += "[b]Eigene[/b]\n";
                        activeList = holder.getOwnTroops();
                        break;
                    case TroopsManagerTableModel.SHOW_TROOPS_IN_VILLAGE:
                        result += "[b]Im Dorf[/b]\n";
                        activeList = holder.getTroopsInVillage();
                        break;
                    case TroopsManagerTableModel.SHOW_TROOPS_OUTSIDE:
                        result += "[b]Außerhalb[/b]\n";
                        activeList = holder.getTroopsOutside();
                        break;
                    case TroopsManagerTableModel.SHOW_TROOPS_ON_THE_WAY:
                        result += "[b]Unterwegs[/b]\n";
                        activeList = holder.getTroopsOnTheWay();
                        break;
                    case TroopsManagerTableModel.SHOW_FORGEIGN_TROOPS:
                        result += "[b]Unterstützung[/b]\n";
                        activeList = holder.getForeignTroops();
                        break;
                }
                result += buildTroopsInformation(activeList, pServerURL, pExtended);
            } else {
                result += "[b]Eigene[/b]\n";
                result += buildTroopsInformation(holder.getOwnTroops(), pServerURL, pExtended);
                result += "\n[b]Im Dorf[/b]\n";
                result += buildTroopsInformation(holder.getTroopsInVillage(), pServerURL, pExtended);
                result += "\n[b]Außerhalb[/b]\n";
                result += buildTroopsInformation(holder.getTroopsOutside(), pServerURL, pExtended);
                result += "\n[b]Unterwegs[/b]\n";
                result += buildTroopsInformation(holder.getTroopsOnTheWay(), pServerURL, pExtended);
                result += "\n[b]Unterstützung[/b]\n";
                result += buildTroopsInformation(holder.getForeignTroops(), pServerURL, pExtended);
            }
        } else {
            result += " Es liegen keine Informationen vor\n";
        }*/
        return "";
    }

    private static String buildTroopsInformation(Hashtable<UnitHolder, Integer> pUnits, String pServerURL, boolean pExtended) {
        String result = " ";
        if (pUnits == null) {
            result = " Es liegen keine Informationen vor\n";
        } else {
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                if (pExtended) {
                    result += pUnits.get(unit) + " [img]" + pServerURL + "/graphic/unit/unit_" + unit.getPlainName() + ".png[/img] ";
                } else {
                    result += pUnits.get(unit) + " " + unit.getName() + "\n";
                }
            }
            result = result.substring(0, result.length() - 1);
        }
        return result + "\n";
    }
}
