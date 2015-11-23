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
package de.tor.tribes.util.troops;

import de.tor.tribes.types.NoTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.tag.TagManager;
import java.util.List;

/**
 *
 * @author Charon
 */
public class TroopsTagFilter implements TroopsFilterInterface {

    private List<Tag> validTags = null;

    @Override
    public void setup(Object pFilterComponent) {
        validTags = (List<Tag>) pFilterComponent;
    }

    @Override
    public boolean isValid(Village pVillage) {
        if (validTags == null || validTags.isEmpty()) {
            return true;
        }
        //check NoTag
        if (validTags.contains(NoTag.getSingleton())) {
            if (TagManager.getSingleton().getTags(pVillage).isEmpty()) {
                return true;
            }
        }

        //check if any tag tags the village
        for (Tag t : validTags) {
            if (t.tagsVillage(pVillage.getId())) {
                return true;
            }
        }
        return false;
    }
}
