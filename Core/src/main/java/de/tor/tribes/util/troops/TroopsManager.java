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

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.xml.JDomUtils;
import java.awt.*;
import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 * @author Charon
 */
public class TroopsManager extends GenericManager<VillageTroopsHolder> {

    public enum TROOP_TYPE {

        IN_VILLAGE, OWN, OUTWARDS, ON_THE_WAY, SUPPORT
    }
    public static final String IN_VILLAGE_GROUP = "Im Dorf";
    public static final String OWN_GROUP = "Eigene";
    public static final String OUTWARDS_GROUP = "Außerhalb";
    public static final String ON_THE_WAY_GROUP = "Unterwegs";
    public static final String SUPPORT_GROUP = "Unterstützung";
    private static Logger logger = LogManager.getLogger("TroopsManager");
    private static TroopsManager SINGLETON = null;
    private HashMap<String, HashMap<Village, VillageTroopsHolder>> managedElementGroups = new HashMap<>();
    private List<Image> mTroopMarkImages = new LinkedList<>();

    public static synchronized TroopsManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TroopsManager();
        }
        return SINGLETON;
    }

    TroopsManager() {
        super(IN_VILLAGE_GROUP, true);
        try {
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/off_marker.png")));
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/def_marker.png")));
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/def_cav_marker.png")));
            mTroopMarkImages.add(ImageIO.read(new File("graphics/icons/def_arch_marker.png")));
        } catch (Exception e) {
            logger.error("Failed to read troops markers", e);
        }
    }

    @Override
    public Iterator<String> getGroupIterator() {
        return managedElementGroups.keySet().iterator();
    }

    @Override
    public String[] getGroups() {
        return new String[]{IN_VILLAGE_GROUP, OWN_GROUP, OUTWARDS_GROUP, ON_THE_WAY_GROUP, SUPPORT_GROUP};
    }

    @Override
    public List<ManageableType> removeGroup(String pGroup) {
        return new LinkedList<>();
    }

    @Override
    public boolean addGroup(String pGroup) {
        return false;
    }

    @Override
    public boolean renameGroup(String pOldName, String pNewName) {
        return false;
    }

    @Override
    public void removeAllElementsFromGroup(String pGroup) {
    }

    @Override
    public VillageTroopsHolder getManagedElement(int pIndex) {
        return getManagedElement(getDefaultGroupName(), pIndex);
    }

    @Override
    public VillageTroopsHolder getManagedElement(String pGroup, int pIndex) {
        return (VillageTroopsHolder) managedElementGroups.get(pGroup).values().toArray()[pIndex];
    }

    @Override
    public List<ManageableType> getAllElementsFromAllGroups() {
        return getAllElements(Arrays.asList(getGroups()));
    }

    @Override
    public List<ManageableType> getAllElements(final List<String> pGroups) {
        List<ManageableType> elements = new LinkedList<>();
        for (String group : pGroups) {
            for (ManageableType t : getAllElements(group)) {
                elements.add(t);
            }
        }
        return Collections.unmodifiableList(elements);
    }

    @Override
    public List<ManageableType> getAllElements() {
        return getAllElements(getDefaultGroupName());
    }

    @Override
    public List<ManageableType> getAllElements(String pGroup) {
        HashMap<Village, VillageTroopsHolder> set = managedElementGroups.get(pGroup);
        if (set == null) {
            return new LinkedList<>();
        }
        Collection<VillageTroopsHolder> values = set.values();
        if (values == null) {
            return new LinkedList<>();
        }
        return Collections.unmodifiableList(Arrays.asList(set.values().toArray(new ManageableType[set.size()])));
    }

    public boolean hasInformation(TROOP_TYPE pType) {
        HashMap<Village, VillageTroopsHolder> info = managedElementGroups.get(getGroupForType(pType));
        return info != null && !info.isEmpty();
    }

    @Override
    public boolean groupExists(String pGroup) {
        return managedElementGroups.containsKey(pGroup);
    }

    public String getGroupForType(TROOP_TYPE pType) {
        String group = null;
        switch (pType) {
            case ON_THE_WAY:
                group = ON_THE_WAY_GROUP;
                break;
            case OUTWARDS:
                group = OUTWARDS_GROUP;
                break;
            case OWN:
                group = OWN_GROUP;
                break;
            case SUPPORT:
                group = SUPPORT_GROUP;
                break;
            default:
                group = IN_VILLAGE_GROUP;
                break;
        }
        return group;
    }

    public int getElementCount(TROOP_TYPE pType) {
        return getElementCount(getGroupForType(pType));
    }

    @Override
    public int getElementCount() {
        return getElementCount(getDefaultGroupName());
    }

    @Override
    public int getElementCount(String pGroup) {
        return managedElementGroups.get(pGroup).size();
    }

    @Override
    public void addManagedElement(VillageTroopsHolder pElement) {
        addManagedElement(getDefaultGroupName(), pElement);
    }

    @Override
    public void addManagedElement(String pGroup, VillageTroopsHolder pElement) {
        boolean changed = false;
        if (pElement == null || pElement.getVillage() == null) {
            return;
        }

        HashMap<Village, VillageTroopsHolder> elems = managedElementGroups.get(pGroup);
        elems.put(pElement.getVillage(), pElement);
        changed = true;

        if (changed) {
            fireDataChangedEvents(pGroup);
        }
    }

    @Override
    public void removeElement(VillageTroopsHolder pElement) {
        removeElement(getDefaultGroupName(), pElement);
    }

    @Override
    public void removeElement(String pGroup, VillageTroopsHolder pElement) {
        boolean changed = false;
        if (pElement == null) {
            return;
        }
        HashMap<Village, VillageTroopsHolder> set = managedElementGroups.get(pGroup);
        if (set == null) {
            return;
        }

        changed = set.remove(pElement.getVillage()) != null;

        if (changed) {
            fireDataChangedEvents(pGroup);
        }
    }

    @Override
    public void removeElements(List<VillageTroopsHolder> pElements) {
        removeElements(getDefaultGroupName(), pElements);
    }

    @Override
    public void removeElements(String pGroup, List<VillageTroopsHolder> pElements) {
        if (pElements == null || pElements.isEmpty()) {
            return;
        }

        invalidate();
        for (ManageableType element : pElements) {
            removeElement(pGroup, (VillageTroopsHolder) element);
        }
        revalidate();
        fireDataChangedEvents(pGroup);
    }

    public VillageTroopsHolder getTroopsForVillage(Village pVillage) {
        return getTroopsForVillage(pVillage, TROOP_TYPE.IN_VILLAGE);
    }

    public VillageTroopsHolder getTroopsForVillage(Village pVillage, TROOP_TYPE pType) {
        return getTroopsForVillage(pVillage, pType, false);
    }

    public VillageTroopsHolder getTroopsForVillage(Village pVillage, TROOP_TYPE pType, boolean pCreate) {
        String group = null;
        switch (pType) {
            case ON_THE_WAY:
                group = ON_THE_WAY_GROUP;
                break;
            case OUTWARDS:
                group = OUTWARDS_GROUP;
                break;
            case OWN:
                group = OWN_GROUP;
                break;
            case SUPPORT:
                group = SUPPORT_GROUP;
                break;
            default:
                group = IN_VILLAGE_GROUP;
                break;
        }
        for (ManageableType t : getAllElements(group)) {
            if (((VillageTroopsHolder) t).getVillage().equals(pVillage)) {
                return (VillageTroopsHolder) t;
            }
        }
        if (pCreate) {
            VillageTroopsHolder newHolder = null;
            if (pType.equals(TROOP_TYPE.SUPPORT)) {
                newHolder = new SupportVillageTroopsHolder(pVillage, new Date());
            } else {
                newHolder = new VillageTroopsHolder(pVillage, new Date());
            }
            addManagedElement(group, newHolder);
            return newHolder;
        } else {
            return null;
        }
    }

    public Image getTroopsMarkerForVillage(Village pVillage) {
        VillageTroopsHolder inVillage = getTroopsForVillage(pVillage);
        if (inVillage == null) {
            return null;
        }
        List<Double> l = new LinkedList<>();
        double off = inVillage.getTroops().getOffValue();
        double def = inVillage.getTroops().getDefValue();
        double defCav = inVillage.getTroops().getDefCavalryValue();
        double defArch = inVillage.getTroops().getDefArcherValue();

        l.add(off);
        l.add(def);
        l.add(defCav);
        l.add(defArch);

        Collections.sort(l);

        double max = l.get(3);
        if (max == off) {
            return mTroopMarkImages.get(0);
        } else if (max == def) {
            return mTroopMarkImages.get(1);
        } else if (max == defCav) {
            return mTroopMarkImages.get(2);
        }
        //archer def must be max.
        return mTroopMarkImages.get(3);
    }

    public int getEntryCount(Tag[] tags, boolean pANDConnection, TROOP_TYPE pType) {
        if (tags == null) {
            return getElementCount(pType);
        }

        Iterator<Village> keys = managedElementGroups.get(getGroupForType(pType)).keySet().iterator();
        List<Village> valid = new LinkedList<>();
        while (keys.hasNext()) {
            Village key = keys.next();
            boolean isValid = pANDConnection;
            for (Tag t : tags) {
                if (t.tagsVillage(key.getId()) && !pANDConnection) {
                    //at least one tag was valid
                    isValid = true;
                    break;
                } else if (!t.tagsVillage(key.getId()) && pANDConnection) {
                    //if AND connection is selected and village is not tagges, break and skip adding
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                //add village if not already in and valid following the logical operation
                if (!valid.contains(key)) {
                    valid.add(key);
                }
            } else {
                //remove if village was already added
                valid.remove(key);
            }
        }

        return valid.size();
    }

    public Village[] getVillages(Tag[] tags, boolean pANDConnection, TROOP_TYPE pType) {
        if (tags == null) {
            return new Village[0];
        }

        Iterator<Village> keys = managedElementGroups.get(getGroupForType(pType)).keySet().iterator();
        List<Village> valid = new LinkedList<>();
        while (keys.hasNext()) {
            Village key = keys.next();
            boolean isValid = pANDConnection;
            for (Tag t : tags) {
                if (t.tagsVillage(key.getId()) && !pANDConnection) {
                    //at least one tag was valid
                    isValid = true;
                    break;
                } else if (!t.tagsVillage(key.getId()) && pANDConnection) {
                    //if AND connection is selected and village is not tagges, break and skip adding
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                //add village if not already in and valid following the logical operation
                if (!valid.contains(key)) {
                    valid.add(key);
                }
            } else {
                //remove if village was already added
                valid.remove(key);
            }
        }

        return valid.toArray(new Village[]{});
    }

    @Override
    public final void initialize() {
        if (managedElementGroups == null) {
            managedElementGroups = new HashMap<>();
        } else {
            managedElementGroups.clear();
        }
        managedElementGroups.put(IN_VILLAGE_GROUP, new HashMap<Village, VillageTroopsHolder>());
        managedElementGroups.put(OWN_GROUP, new HashMap<Village, VillageTroopsHolder>());
        managedElementGroups.put(OUTWARDS_GROUP, new HashMap<Village, VillageTroopsHolder>());
        managedElementGroups.put(ON_THE_WAY_GROUP, new HashMap<Village, VillageTroopsHolder>());
        managedElementGroups.put(SUPPORT_GROUP, new HashMap<Village, VillageTroopsHolder>());
    }

    @Override
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        invalidate();

        logger.info("Loading troops");
        try {
            for (Element e : (List<Element>) JDomUtils.getNodes(pElm, "troopGroups/troopGroup")) {
                String groupKey = e.getAttributeValue("name");
                groupKey = URLDecoder.decode(groupKey, "UTF-8");
                logger.debug("Loading troops from group '" + groupKey + "'");
                
                for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "troopInfos/troopInfo")) {
                    String type = e1.getAttributeValue("type");
                    VillageTroopsHolder holder = null;
                    if (type != null && type.equals("support")) {
                        holder = new SupportVillageTroopsHolder();
                    } else {
                        holder = new VillageTroopsHolder();
                    }
                    holder.loadFromXml(e1);
                    addManagedElement(groupKey, holder);
                    result++;
                }
            }
            logger.debug("Troops loaded successfully");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load troops", e);
        }
        revalidate(true);
        return result;

    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element troopGroups = new Element("troopGroups");
        if (pGroupsToExport == null || pGroupsToExport.isEmpty()) {
            return troopGroups;
        }
        try {
            logger.debug("Generating troop data");
            for (String group : pGroupsToExport) {
                Element troopGroup = new Element("troopGroup");
                troopGroup.setAttribute("name", URLEncoder.encode(group, "UTF-8"));
                
                Element troopInfos = new Element("troopInfos");
                for (ManageableType t : getAllElements(group)) {
                    troopInfos.addContent(t.toXml("troopInfo"));
                }
                troopGroup.addContent(troopInfos);
                troopGroups.addContent(troopGroup);
            }
            logger.debug("Data generated successfully");
        } catch (Exception e) {
            logger.error("Failed to generate troop data", e);
        }
        return troopGroups;
    }
}
