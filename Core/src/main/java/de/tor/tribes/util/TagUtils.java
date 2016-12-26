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
package de.tor.tribes.util;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
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
        List<Tag> result = new LinkedList<>();
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
        List<Tag> result = new LinkedList<>();
        for (Tag tag : tags) {
            List<Integer> ids = tag.getVillageIDs();
            for (Integer id : ids) {
                Village tagged = DataHolder.getSingleton().getVillagesById().get(id);
                if (tagged != null && tagged.getTribe() != null && tagged.getTribe().equals(pTribe)) {
                    if (!result.contains(tag)) {//add tag and continue with next
                        result.add(tag);
                    }
                }
            }
        }

        if (pComparator != null) {
            Collections.sort(result, pComparator);
        }
        result.add(0, NoTag.getSingleton());
        return result.toArray(new Tag[result.size()]);
    }
}
