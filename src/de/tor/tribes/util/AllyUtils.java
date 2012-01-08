/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.Barbarians;
import de.tor.tribes.types.ext.NoAlly;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
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
public class AllyUtils {

    public static Ally[] getAlliesByFilter(final String pFilter, Comparator<Ally> pComparator) {
        if (pFilter == null) {
            return new Ally[0];
        }
        final String filter = pFilter.toLowerCase();

        List<Ally> allies = new LinkedList<Ally>();
        Enumeration<Ally> keys = DataHolder.getSingleton().getAllies().elements();
        while (keys.hasMoreElements()) {
            allies.add(keys.nextElement());
        }
        CollectionUtils.filter(allies, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return pFilter.length() == 0 || ((Ally) o).getName().toLowerCase().indexOf(filter) >= 0 || ((Ally) o).getTag().toLowerCase().indexOf(filter) >= 0;
            }
        });

        Ally[] result = allies.toArray(new Ally[allies.size()]);
        if (pComparator != null) {
            Arrays.sort(result, pComparator);
        }
        result = (Ally[]) ArrayUtils.add(result, 0, NoAlly.getSingleton());
        return result;
    }

    public static Ally[] filterAllies(Ally[] pAllies, String pFilter, Comparator<Ally> pComparator) {
        List<Ally> result = new LinkedList<Ally>();
        if (pFilter == null || pFilter.length() == 0) {
            Collections.addAll(result, pAllies);
        } else {
            String filter = pFilter.toLowerCase();
            for (Ally a : pAllies) {
                if (a.getName().toLowerCase().indexOf(filter) >= 0 || a.getTag().toLowerCase().indexOf(filter) >= 0) {
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
            List<Tribe> tribes = new LinkedList<Tribe>();
            Enumeration<Tribe> keys = DataHolder.getSingleton().getTribes().elements();
            while (keys.hasMoreElements()) {
                tribes.add(keys.nextElement());
            }

            CollectionUtils.filter(tribes, new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    return ((Tribe) o).getAlly() == null || ((Tribe) o).getAlly().equals(NoAlly.getSingleton());
                }
            });
            result = tribes.toArray(new Tribe[tribes.size()]);
        } else {
            result = pAlly.getTribes().toArray(new Tribe[pAlly.getMembers()]);
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
        List<Ally> allies = new LinkedList<Ally>();
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
