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
import de.tor.tribes.types.ext.*;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/**
 *
 * @author Torridity
 */
public class AllyUtils {

    public static Ally[] getAlliesByFilter(final String pFilter, Comparator<Ally> pComparator) {
        if (pFilter == null) {
            return new Ally[0];
        }
        final String filter = pFilter.toLowerCase();

        List<Ally> allies = new LinkedList<>();
        CollectionUtils.addAll(allies, DataHolder.getSingleton().getAllies().values());

        if (filter.length() > 0) {
            CollectionUtils.filter(allies, new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    return pFilter.length() == 0 || ((Ally) o).getName().toLowerCase().contains(filter) || ((Ally) o).getTag().toLowerCase().contains(filter);
                }
            });
        }

        if (pComparator != null) {
            Collections.sort(allies, pComparator);
        }
        allies.add(0, NoAlly.getSingleton());
        //result = (Ally[]) ArrayUtils.add(result, 0, NoAlly.getSingleton());
        return allies.toArray(new Ally[allies.size()]);
    }

    public static Ally[] filterAllies(Ally[] pAllies, String pFilter, Comparator<Ally> pComparator) {
        List<Ally> result = new LinkedList<>();
        if (pFilter == null || pFilter.length() == 0) {
            Collections.addAll(result, pAllies);
        } else {
            String filter = pFilter.toLowerCase();
            for (Ally a : pAllies) {
                if (a.getName().toLowerCase().contains(filter) || a.getTag().toLowerCase().contains(filter)) {
                    result.add(a);
                }
            }
        }

        if (pComparator != null) {
            Collections.sort(result, pComparator);
        }

        return result.toArray(new Ally[result.size()]);
    }

    public static Tribe[] getTribes(Ally pAlly, Comparator<Tribe> pComparator) {
        Tribe[] result = null;
        if (pAlly == null) {
            return new Tribe[0];
        } else if (pAlly.equals(NoAlly.getSingleton())) {
            List<Tribe> tribes = new LinkedList<>();
            CollectionUtils.addAll(tribes, DataHolder.getSingleton().getTribes().values());
            CollectionUtils.filter(tribes, new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    return ((Tribe) o).getAlly() == null || ((Tribe) o).getAlly().equals(NoAlly.getSingleton());
                }
            });
            result = tribes.toArray(new Tribe[tribes.size()]);
        } else {
            result = pAlly.getTribes();
        }

        if (pComparator != null) {
            Arrays.sort(result, pComparator);
        }

        return result;
    }

    public static Ally[] getAlliesByVillage(Village pVillage, boolean pIncludeBarbarians, Comparator<Ally> pComparator) {
        return getAlliesByVillage(new Village[]{pVillage}, pIncludeBarbarians, pComparator);
    }

    public static Ally[] getAlliesByVillage(Village[] pVillages, boolean pIncludeBarbarians, Comparator<Ally> pComparator) {
        List<Ally> allies = new LinkedList<>();
        if (pVillages == null || pVillages.length == 0) {
            Set<Entry<Integer, Ally>> entries = DataHolder.getSingleton().getAllies().entrySet();
            for (Entry<Integer, Ally> entry : entries) {
                allies.add(entry.getValue());
            }
        } else {
            for (Village v : pVillages) {
                Tribe t = v.getTribe();
                if (pIncludeBarbarians || !t.equals(Barbarians.getSingleton())) {
                    Ally a = t.getAlly();
                    if (a == null) {
                        a = NoAlly.getSingleton();
                    }
                    if (!allies.contains(a)) {
                        allies.add(a);
                    }
                }
            }
        }
        if (pComparator != null) {
            Collections.sort(allies, pComparator);
        }

        return allies.toArray(new Ally[allies.size()]);
    }
}
