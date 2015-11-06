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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Torridity
 */
public class VillageUtils {

    public enum RELATION {

        AND, OR
    };

    public static Village[] getVillages(Tribe pTribe) {
        return getVillages(new Tribe[0]);
    }

    public static Village[] getVillages(Tribe[] pTribes) {
        List<Village> villageList = new LinkedList<Village>();
        if (pTribes == null || pTribes.length == 0) {
            Set<Entry<Integer, Village>> entries = DataHolder.getSingleton().getVillagesById().entrySet();
            for (Entry<Integer, Village> entry : entries) {
                villageList.add(entry.getValue());
            }
        } else {
            for (Tribe t : pTribes) {
                Collections.addAll(villageList, t.getVillageList());
            }
        }
        return villageList.toArray(new Village[villageList.size()]);
    }

    public static Village[] getVillagesTag(Tag pTag, Tribe pTribe, RELATION pRelation, boolean pWithBarbarians, Comparator pComparator) {
        if (pTag == null) {
            return new Village[0];
        }
        return getVillagesByTag(new Tag[]{pTag}, pTribe, pRelation, pWithBarbarians, pComparator);
    }

    public static Village[] getVillagesByTag(Tag[] pTags, Tribe pTribe, RELATION pRelation, boolean pWithBarbarians, Comparator pComparator) {
        if (pTags == null) {
            return new Village[0];
        }
        List<Village> villages = new ArrayList<Village>();
        Hashtable<Village, Integer> usageCount = new Hashtable<Village, Integer>();
        for (Tag tag : pTags) {
            for (Integer id : tag.getVillageIDs()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                if (pWithBarbarians || !v.getTribe().equals(Barbarians.getSingleton())) {
                    if (pTribe == null || v.getTribe().getId() == pTribe.getId()) {
                        usageCount.put(v, (usageCount.get(v) == null) ? 1 : usageCount.get(v) + 1);
                        if (!villages.contains(v)) {
                            villages.add(v);
                        }
                    }
                }
            }
        }
        if (pRelation.equals(RELATION.AND)) {
            //remove villages that are tagges by less tags than tagCount
            Enumeration<Village> keys = usageCount.keys();
            int tagAmount = pTags.length;
            while (keys.hasMoreElements()) {
                Village key = keys.nextElement();
                Integer count = usageCount.get(key);
                if (count == null || count != tagAmount) {
                    villages.remove(key);
                }
            }
        }

        if (pComparator != null) {
            Collections.sort(villages, pComparator);
        }

        return villages.toArray(new Village[villages.size()]);
    }

    public static Village[] getVillagesByTag(Tag pTag, Tribe pTribe, Comparator pComparator) {
        return getVillagesByTag(new Tag[]{pTag}, pTribe, RELATION.OR, false, pComparator);
    }

    public static Village[] getVillagesByTag(Tag[] pTags, Comparator pComparator) {
        return getVillagesByTag(pTags, null, RELATION.OR, false, pComparator);
    }

    public static Village[] getVillagesByTag(Tag[] pTags, Tribe pTribe, Comparator pComparator) {
        return getVillagesByTag(pTags, pTribe, RELATION.OR, false, pComparator);
    }

    public static Village[] getVillagesByContinent(Village[] pVillages, final Integer pContinent, Comparator<Village> pComparator) {
        return getVillagesByContinent(pVillages, new Integer[]{pContinent}, pComparator);
    }

    public static Village[] getVillagesByContinent(Village[] pVillages, final Integer[] pContinents, Comparator<Village> pComparator) {
        if (pContinents == null || pContinents.length == 0) {
            return pVillages;
        }
        List<Village> villages = new LinkedList<Village>();
        Collections.addAll(villages, pVillages);

        CollectionUtils.filter(villages, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return ArrayUtils.contains(pContinents, ((Village) o).getContinent());
            }
        });
        if (pComparator != null) {
            Collections.sort(villages, pComparator);
        }
        return villages.toArray(new Village[villages.size()]);
    }

    public static String[] getContinents(Village[] pVillages) {
        List<String> continents = new ArrayList<String>();

        for (Village v : pVillages) {
            int cont = v.getContinent();
            String sCont = "K" + ((cont < 10) ? "0" + cont : cont);
            if (!continents.contains(sCont)) {
                continents.add(sCont);
            }
        }
        Collections.sort(continents, String.CASE_INSENSITIVE_ORDER);
        return continents.toArray(new String[continents.size()]);
    }

    public static String[] getContinents(Tribe pTribe) {
        if (pTribe == null || pTribe.equals(Barbarians.getSingleton())) {
            return new String[0];
        }
        List<String> continents = new ArrayList<String>();

        for (Village v : pTribe.getVillageList()) {
            int cont = v.getContinent();
            String sCont = "K" + ((cont < 10) ? "0" + cont : cont);
            if (!continents.contains(sCont)) {
                continents.add(sCont);
            }
        }
        Collections.sort(continents, String.CASE_INSENSITIVE_ORDER);
        return continents.toArray(new String[continents.size()]);
    }

    public static String[] getContinentsByTag(Tag[] pTags) {
        return getContinents(getVillagesByTag(pTags, null));
    }

    public static Village[] getVillagesByAlly(Ally[] pAllies, Comparator<Village> pComparator) {
        List<Village> villages = new LinkedList<Village>();

        for (Ally a : pAllies) {
            Tribe[] tribes = AllyUtils.getTribes(a, null);
            for (Tribe t : tribes) {
                if (t != null && t.getVillageList() != null) {
                    Collections.addAll(villages, t.getVillageList());
                }
            }
        }

        if (pComparator != null) {
            Collections.sort(villages, pComparator);
        }
        return villages.toArray(new Village[villages.size()]);
    }
}
