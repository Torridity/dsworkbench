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
package de.tor.tribes.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @param <C>
 * @author Torridity
 */
public abstract class GenericManager<C extends ManageableType> {

    public static final String DEFAULT_GROUP = "default";
    private String alternateDefaultGroupName = null;
    private List<GenericManagerListener> listeners = new ArrayList<>();
    private HashMap<String, List<ManageableType>> managedElementGroups = new HashMap<>();
    private boolean groupable = false;
    private boolean valid = true;

    /**
     * Default non-groupable constructor
     */
    public GenericManager() {
        this(null, false);
    }

    /**
     * Default groupable constructor
     *
     * @param pGroupable
     */
    public GenericManager(boolean pGroupable) {
        this(null, pGroupable);
    }

    public String getDefaultGroupName() {
        return alternateDefaultGroupName;
    }

    public GenericManager(String pDefaultGroup, boolean pGroupable) {
        groupable = pGroupable;
        alternateDefaultGroupName = pDefaultGroup;
        initialize();
    }

    /**
     * Add a manager listener
     *
     * @param pListener
     */
    public void addManagerListener(GenericManagerListener pListener) {
        if (!listeners.contains(pListener)) {
            listeners.add(pListener);
        }
    }

    /**
     * Remove a manager listener
     *
     * @param pListener
     */
    public void removeManagerListener(GenericManagerListener pListener) {
        listeners.remove(pListener);
    }

    public void initialize() {
        managedElementGroups.clear();
        managedElementGroups.put(DEFAULT_GROUP, new ArrayList<ManageableType>());
    }

    public Iterator<String> getGroupIterator() {
        return managedElementGroups.keySet().iterator();
    }

    public String[] getGroups() {
        return managedElementGroups.keySet().toArray(new String[managedElementGroups.size()]);
    }

    /**
     * Add a new, empty group
     *
     * @param pGroup
     * @return
     */
    public boolean addGroup(String pGroup) {
        boolean changed = false;
        if (groupable && !managedElementGroups.containsKey(pGroup)) {
            managedElementGroups.put(pGroup, new ArrayList<ManageableType>());
            changed = true;
        }
        if (changed) {
            fireDataChangedEvents();
        }
        return changed;
    }

    /**
     * Remove an entire group
     *
     * @param pGroup
     * @return
     */
    public List<ManageableType> removeGroup(String pGroup) {
        List<ManageableType> result = null;
        boolean changed = false;
        if (groupable && pGroup != null && !pGroup.equals(DEFAULT_GROUP)) {
            List<ManageableType> removedList = managedElementGroups.remove(pGroup);
            if (removedList != null) {
                result = removedList;
                changed = true;
            }
        }
        if (changed) {
            fireDataChangedEvents();
        }

        return (result != null) ? result : new ArrayList<ManageableType>();
    }

    /**
     * Rename a group from pOldName to pNewName
     *
     * @param pOldName
     * @param pNewName
     * @return
     */
    public boolean renameGroup(String pOldName, String pNewName) {
        boolean changed = false;
        if (groupable) {
            List<ManageableType> elementsInOldGroup = managedElementGroups.get(pOldName);
            if (elementsInOldGroup != null) {
                managedElementGroups.remove(pOldName);
                managedElementGroups.put(pNewName, elementsInOldGroup);
                changed = true;
            }
        }

        if (changed) {
            fireDataChangedEvents();
        }
        return changed;
    }

    public void removeAllElementsFromGroup(String pGroup) {
        String group = pGroup;
        if (!groupable) {
            group = DEFAULT_GROUP;
        }
        managedElementGroups.get(group).clear();
        fireDataChangedEvents(group);
    }

    /**
     * Get a specific element located in the default group
     *
     * @param pIndex
     * @return
     */
    public C getManagedElement(int pIndex) {
        return getManagedElement(DEFAULT_GROUP, pIndex);
    }

    /**
     * Get a specific element located in the specified group
     *
     * @param pGroup
     * @param pIndex
     * @return
     */
    public C getManagedElement(String pGroup, int pIndex) {
        if (managedElementGroups.containsKey(pGroup)) {
            return (C) getAllElements(pGroup).get(pIndex);
        }
        return null;
    }

    /**
     * Get all elements from all groups
     *
     * @return
     */
    public List<ManageableType> getAllElementsFromAllGroups() {
        List<ManageableType> allElements = new LinkedList<>();
        for (String group : getGroups()) {
            List<ManageableType> elementsInGroup = getAllElements(group);
            Collections.addAll(allElements, elementsInGroup.toArray(new ManageableType[elementsInGroup.size()]));
        }
        return allElements;
    }

    /**
     * Get all elements located in the provided groups
     *
     * @return
     */
    public List<ManageableType> getAllElements(final List<String> pGroups) {
        List<ManageableType> allElements = new LinkedList<>();
        for (String group : pGroups) {
            Collections.addAll(allElements, getAllElements(group).toArray(new ManageableType[]{}));
        }
        return allElements;
    }

    /**
     * Get all elements located in the default group
     *
     * @return
     */
    public List<ManageableType> getAllElements() {
        return getAllElements(DEFAULT_GROUP);
    }

    /**
     * Get all elements located in the specified group
     *
     * @param pGroup
     * @return
     */
    public List<ManageableType> getAllElements(String pGroup) {
        if (managedElementGroups.containsKey(pGroup)) {
            return Collections.unmodifiableList(managedElementGroups.get(pGroup));
        }
        return new ArrayList<>();
    }

    public boolean groupExists(String pGroup) {
        return managedElementGroups.containsKey(pGroup);
    }

    /**
     * Get the amount of elements within the default group
     *
     * @return
     */
    public int getElementCount() {
        return getElementCount(DEFAULT_GROUP);
    }

    /**
     * Get the amount of elements within the specific group
     *
     * @param pGroup
     * @return
     */
    public int getElementCount(String pGroup) {
        int result = 0;
        if (!groupable) {
            result = managedElementGroups.get(DEFAULT_GROUP).size();
        } else {
            if (managedElementGroups.containsKey(pGroup)) {
                result = managedElementGroups.get(pGroup).size();
            }
        }
        return result;
    }

    /**
     * Adds an element to the default group
     *
     * @param pElement
     */
    public void addManagedElement(C pElement) {
        addManagedElement(DEFAULT_GROUP, pElement);
    }

    /**
     * Adds an element to a specific group
     *
     * @param pGroup
     * @param pElement
     */
    public void addManagedElement(String pGroup, C pElement) {
        boolean changed;
        boolean structureChanged = false;
        if (pElement == null) {
            return;
        }
        if (!groupable || pGroup == null) {
            managedElementGroups.get(DEFAULT_GROUP).add(pElement);
            changed = true;
        } else {
            List<ManageableType> elems = managedElementGroups.get(pGroup);
            if (elems == null) {
                elems = new ArrayList<>();
                managedElementGroups.put(pGroup, elems);
                structureChanged = true;
            }
            elems.add(pElement);
            changed = true;
        }
        if (changed && !structureChanged) {
            fireDataChangedEvents(pGroup);
        } else if (changed && structureChanged) {
            fireDataChangedEvents();
        }
    }

    /**
     * Removes an element from the default group
     *
     * @param pElement
     */
    public void removeElement(C pElement) {
        removeElement(DEFAULT_GROUP, pElement);
    }

    /**
     * Removes an element from a specific group
     *
     * @param pGroup
     * @param pElement
     */
    public void removeElement(String pGroup, C pElement) {
        boolean changed = false;
        if (!groupable) {
            managedElementGroups.get(DEFAULT_GROUP).remove(pElement);
            changed = true;
        } else {
            if (managedElementGroups.containsKey(pGroup)) {
                managedElementGroups.get(pGroup).remove(pElement);
                changed = true;
            }
        }
        if (changed) {
            fireDataChangedEvents(pGroup);
        }
    }

    /**
     * Removes a list of element from the default group
     *
     * @param pElements
     */
    public void removeElements(List<C> pElements) {
        removeElements(DEFAULT_GROUP, pElements);
    }

    /**
     * Removes a list of element from the default group
     *
     * @param pGroup
     * @param pElements
     */
    public void removeElements(String pGroup, List<C> pElements) {
        if (pElements == null || pElements.isEmpty()) {
            return;
        }

        String group = pGroup;
        if (!groupable) {
            group = DEFAULT_GROUP;
        }
        invalidate();
        try {
            for (C element : pElements) {
                removeElement(group, element);
            }
        } finally {
            revalidate();
        }
        fireDataChangedEvents(pGroup);
    }

    public final void invalidate() {
        valid = false;
    }

    public final void revalidate() {
        revalidate(null, false);
    }

    public final void revalidate(boolean pNotify) {
        revalidate(null, pNotify);
    }

    public final void revalidate(String pGroup) {
        revalidate(pGroup, false);
    }

    public final void revalidate(String pGroup, boolean notify) {
        valid = true;
        if (notify) {
            if (pGroup == null) {
                fireDataChangedEvents();
            } else {
                fireDataChangedEvents(pGroup);
            }
        }
    }

    public final void fireDataChangedEvents() {
        fireDataChangedEvents(null);
    }

    public final void fireDataChangedEvents(String pGroup) {
        if (!valid) {
            return;
        }
        String theGroup = pGroup;
        if (!groupable) {
            theGroup = DEFAULT_GROUP;
        }

        for (GenericManagerListener listener : listeners.toArray(new GenericManagerListener[listeners.size()])) {
            if (theGroup == null) {
                listener.dataChangedEvent();
            } else {
                listener.dataChangedEvent(theGroup);
            }
        }

    }

    /////////////////////////////////////////////
    ////Abstract methods that must be implemented
    /////////////////////////////////////////////
    public abstract void loadElements(String pFile);

    public abstract void saveElements(String pFile);

    public abstract String getExportData(final List<String> pGroupsToExport);

    public abstract boolean importData(File pFile, String pExtension);
}
