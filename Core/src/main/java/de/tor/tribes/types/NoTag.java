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
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.io.DataHolder;
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
        List<Integer> ids = new LinkedList<>();
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
