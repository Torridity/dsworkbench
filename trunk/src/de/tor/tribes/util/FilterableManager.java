/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Torridity
 */
public abstract class FilterableManager<C, I extends Filter> {

    private List<C> filteredList = new LinkedList<C>();
    private List<I> filters = new LinkedList<I>();

    public int getFilteredElementCount() {
        return filteredList.size();
    }

    public C getFilteredElement(int pIndex) {
        return filteredList.get(pIndex);
    }

    public void setFilters(List<I> pFilters) {
        filters = new LinkedList<I>(pFilters);
        updateFilters();
    }

    public abstract C[] getUnfilteredElements();

    public void clearFilteredList() {
        filteredList = new LinkedList<C>();
    }

    public List<C> getFilteredList() {
        return filteredList;
    }

    public void updateFilters() {
        C[] aElements = getUnfilteredElements();
        List<C> filtered = new LinkedList<C>();
        for (C c : aElements) {
            if (filters == null || filters.isEmpty()) {
                //no filters defined
                filtered.add(c);
            } else {
                //use all filters
                boolean valid = true;
                for (I f : filters) {
                    if (!f.isValid(c)) {
                        //conquer is invalid for the current filter
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    //only add if conquer is valid for all filters
                    filtered.add(c);
                }
            }
        }
        filteredList = new LinkedList<C>(filtered);
    }
}
