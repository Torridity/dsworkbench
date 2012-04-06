/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.tag.TagManager;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charon
 */
public class NoTag extends Tag {

    private static NoTag SINGLETON = null;

    public static synchronized NoTag getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoTag("Keine Gruppe", false);
        }
        return SINGLETON;
    }

    NoTag(String pName, boolean pShowOnMap) {
        super(pName, pShowOnMap);
    }

    @Override
    public List<Integer> getVillageIDs() {
        List<Integer> ids = new LinkedList<Integer>();
        Tribe user = GlobalOptions.getSelectedProfile().getTribe();
        if (user != null) {
            for (Village v : user.getVillageList()) {
                if (tagsVillage(v.getId())) {
                    ids.add(v.getId());
                }
            }
        }
        return ids;
    }

    @Override
    public boolean tagsVillage(int pVillageID) {
        Village v = DataHolder.getSingleton().getVillagesById().get(pVillageID);
        if (v == null) {
            return false;
        }
        List<Tag> tagList = TagManager.getSingleton().getTags(v);
        return (tagList == null || tagList.isEmpty());
    }

    @Override
    public String toString() {
        return "Keine Gruppe";
    }
}
