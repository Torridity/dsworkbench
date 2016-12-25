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
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

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
    private static Logger logger = Logger.getLogger("TroopsManager");
    private static TroopsManager SINGLETON = null;
    private HashMap<String, HashMap<Village, VillageTroopsHolder>> managedElementGroups = new HashMap<>();
    //  private Hashtable<Village, VillageTroopsHolder> mTroops = null;
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
        double off = inVillage.getOffValue();
        double def = inVillage.getDefValue();
        double defCav = inVillage.getDefCavalryValue();
        double defArch = inVillage.getDefArcherValue();

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
    public void loadElements(String pFile) {
        invalidate();
        initialize();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            revalidate();
            return;
        }

        File troopsFile = new File(pFile);
        if (troopsFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.info("Loading troops from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(troopsFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//troopGroups/troopGroup")) {
                    String groupKey = e.getAttributeValue("name");
                    groupKey = URLDecoder.decode(groupKey, "UTF-8");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading troops from group '" + groupKey + "'");
                    }
                    for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "troopInfos/troopInfo")) {
                        String type = e1.getAttributeValue("type");
                        if (type != null && type.equals("support")) {
                            SupportVillageTroopsHolder holder = new SupportVillageTroopsHolder();
                            holder.loadFromXml(e1);
                            addManagedElement(groupKey, holder);
                        } else {
                            VillageTroopsHolder holder = new VillageTroopsHolder();
                            holder.loadFromXml(e1);
                            addManagedElement(groupKey, holder);
                        }
                    }
                }
                logger.debug("Troops loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load troops", e);
            }
        } else {
            logger.info("No troops found under '" + pFile + "'");
        }
        revalidate();
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        invalidate();
        boolean result = false;
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }

        logger.info("Importing troops");
        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//troopGroups/troopGroup")) {
                String groupKey = e.getAttributeValue("name");
                groupKey = URLDecoder.decode(groupKey, "UTF-8");
                if (logger.isDebugEnabled()) {
                    logger.debug("Loading troops from group '" + groupKey + "'");
                }
                for (Element e1 : (List<Element>) JaxenUtils.getNodes(e, "troopInfos/troopInfo")) {
                    String type = e1.getAttributeValue("type");
                    VillageTroopsHolder holder = null;
                    if (type != null && type.equals("support")) {
                        holder = new SupportVillageTroopsHolder();
                    } else {
                        holder = new VillageTroopsHolder();
                    }
                    holder.loadFromXml(e1);
                    addManagedElement(groupKey, holder);
                }
            }
            logger.debug("Troops imported successfully");
            result = true;
        } catch (Exception e) {
            logger.error("Failed to import troops", e);
        }
        revalidate(true);
        return result;

    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        try {
            logger.debug("Generating troop export data");
            StringBuilder result = new StringBuilder();
            result.append("<troopGroups>\n");
            for (String group : getGroups()) {
                result.append("<troopGroup name=\"").append(URLEncoder.encode(group, "UTF-8")).append("\">\n");
                result.append("<troopInfos>\n");

                ManageableType[] elements = getAllElements(group).toArray(new ManageableType[getAllElements(group).size()]);

                for (ManageableType t : elements) {
                    result.append(t.toXml()).append("\n");
                }
                result.append("</troopInfos>\n");
                result.append("</troopGroup>\n");
            }
            result.append("</troopGroups>\n\n");
            logger.debug("Export data generated successfully");
            return result.toString();
        } catch (Exception e) {
            logger.error("Failed to generate troop export data", e);
            return "";
        }
    }

    @Override
    public void saveElements(String pFile) {
        try {
            StringBuilder b = new StringBuilder();
            b.append("<troopGroups>\n");
            for (String group : getGroups()) {
                b.append("<troopGroup name=\"").append(URLEncoder.encode(group, "UTF-8")).append("\">\n");
                b.append("<troopInfos>\n");
                for (ManageableType t : getAllElements(group)) {
                    b.append(t.toXml()).append("\n");
                }
                b.append("</troopInfos>\n");
                b.append("</troopGroup>\n");
            }
            b.append("</troopGroups>\n\n");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to store troops", e);
        }
    }
}
