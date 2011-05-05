/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Village;
import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jejkal
 */
public class TagToBBCodeFormater {

    public static String formatTag(Tag pTag, String pServerURL, boolean pExtended) {
        String result = "";
        if (pExtended) {
            if (pTag.getTagColor() != null) {
                result = "[u][color=\"#" + Integer.toHexString(pTag.getTagColor().getRGB() & 0x00ffffff) + "\"][b]" + pTag.getName() + "[/b][/color][/u]";
            } else {
                //no color defined
                result = "[u][b]" + pTag.getName() + "[/b][/u]";
            }
        } else {
            result += "[b][u]" + pTag.getName() + "[/u][/b]";
        }
        result += "\n";
        if (pExtended) {
            result += "[quote]";
        }

        //get list of tagged villages
        List<Village> villages = new LinkedList<Village>();
        for (Integer id : pTag.getVillageIDs()) {
            villages.add(DataHolder.getSingleton().getVillagesById().get(id));
        }
        //sort villages and add to result
        Collections.sort(villages);
        for (Village v : villages) {
            result += v.toBBCode() + "\n";
        }
        if (pExtended) {
            result += "[/quote]";
        }
        result += "\n";
        return result;
    }
    
    public static void main(String[] args) {
        Tag t = new Tag("Test", true);
        t.setTagIcon(-1);
        t.setTagColor(Color.RED);
        System.out.println(        TagToBBCodeFormater.formatTag(t,"test", true));
    }
}
