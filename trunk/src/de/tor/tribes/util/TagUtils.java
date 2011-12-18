/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.tag.TagManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public class TagUtils {

    public static Tag[] getTags(Comparator<Tag> pComparator) {
        List<Tag> result = new LinkedList<Tag>();
        for (ManageableType type : TagManager.getSingleton().getAllElements()) {
            result.add((Tag) type);
        }

        if (pComparator != null) {
            Collections.sort(result, pComparator);
        }

        result.add(0, NoTag.getSingleton());
        return result.toArray(new Tag[result.size()]);
    }

    public static Tag[] getTagsByVillage(Village pVillage, Comparator<Tag> pComparator) {
        List<Tag> tags = TagManager.getSingleton().getTags(pVillage);
        if (pComparator != null) {
            Collections.sort(tags, pComparator);
        }
        return tags.toArray(new Tag[tags.size()]);
    }

    public static Tag[] getTagsByTribe(Tribe pTribe, Comparator<Tag> pComparator) {
        Tag[] tags = getTags(null);
        List<Tag> result = new LinkedList<Tag>();
        for (Tag tag : tags) {
            if (VillageUtils.getVillagesByTag(tag, pTribe, null).length > 0) {
                result.add(tag);
            };
        }
        if (pComparator != null) {
            Collections.sort(result, pComparator);
        }
        return result.toArray(new Tag[result.size()]);
    }
}
