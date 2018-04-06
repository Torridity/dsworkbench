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
package de.tor.tribes.util.tag;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.LinkedTag;
import de.tor.tribes.types.Tag;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.TagUtils;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import org.jdom.Document;
import org.jdom.Element;

/**Manager for village tags. Tags can be stored in files or in a database (not implemented yet)
 * @author Torridity
 */
public class TagManager extends GenericManager<Tag> {

    private static Logger logger = Logger.getLogger("TagManager");
    private static TagManager SINGLETON = null;

    public static synchronized TagManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TagManager();
        }
        return SINGLETON;
    }

    TagManager() {
        super(false);
    }

    @Override
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        invalidate();
        initialize();
        File tagFile = new File(pFile);
        if (tagFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading tags from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(tagFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//tags/tag")) {
                    Tag t = null;
                    if (e.getChild("equation") != null) {
                        t = new LinkedTag();
                        t.loadFromXml(e);
                    } else {
                        t = new Tag();
                        t.loadFromXml(e);
                    }
                    addManagedElement(t);
                }
                logger.debug("Tags loaded successfully");
            } catch (Exception e) {
                logger.error("Failed to load tags", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("No tags found under '" + pFile + "'");
            }
        }
        revalidate(true);
    }

    public void updateLinkedTags() {
        //update linked tags
        invalidate();
        for (ManageableType e : getAllElements()) {
            if (e instanceof LinkedTag) {
                ((LinkedTag) e).updateVillageList();
            }
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
        logger.debug("Importing tags");
        try {
            Document d = JaxenUtils.getDocument(pFile);
            for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//tags/tag")) {
                Tag t = null;
                if (e.getChild("equation") != null) {
                    t = new LinkedTag();
                    t.loadFromXml(e);
                } else {
                    t = new Tag();
                    t.loadFromXml(e);
                }

                if (pExtension != null) {
                    t.setName(t.getName() + "_" + pExtension);
                }
                Tag existing = getTagByName(t.getName());
                if (existing == null) {
                    //add new tag
                    addManagedElement(t);
                } else {
                    //set tag for new villages
                    for (Integer villageID : t.getVillageIDs()) {
                        existing.tagVillage(villageID);
                    }
                }
            }

            logger.debug("Tags imported successfully");
            result = true;
        } catch (Exception e) {
            logger.error("Failed to load tags", e);
        }
        revalidate(true);
        return result;
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        try {
            logger.debug("Generating tag export data");

            String result = "<tags>\n";
            ManageableType[] elements = getAllElements().toArray(new ManageableType[getAllElements().size()]);

            for (ManageableType e : elements) {
                Tag t = (Tag) e;
                result += t.toXml();
            }
            result += "</tags>\n";
            logger.debug("Export data generated successfully");
            return result;
        } catch (Exception e) {
            logger.error("Failed to generate export data for tags", e);
            return "";
        }
    }

    @Override
    public void saveElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
        }
        try {
            logger.debug("Writing tags to '" + pFile + "'");

            StringBuilder b = new StringBuilder();
            b.append("<tags>\n");
            for (ManageableType e : getAllElements()) {
                Tag t = (Tag) e;
                b.append(t.toXml());
            }
            b.append("</tags>\n");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("Tags successfully saved");
        } catch (Exception e) {
            logger.error("Failed to store tags", e);
        }
    }

    /**Get all tags for a village*/
    public synchronized List<Tag> getTags(Village pVillage) {
        if (pVillage == null) {
            return new LinkedList<>();
        }
        List<Tag> tags = new LinkedList<>();
        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            if (t.tagsVillage(pVillage.getId())) {
                tags.add(t);
            }
        }
        return tags;
    }

    public Tag getTagByName(String pName) {
        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            if (t.getName().equals(pName)) {
                return t;
            }
        }
        return null;
    }

    public void removeTagByName(String pName) {
        Tag toRemove = null;
        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            if (t.getName().equals(pName)) {
                toRemove = t;
                break;
            }
        }
        if (toRemove != null) {
            removeElement(toRemove);
        }
    }

    public void removeTagFastByName(String pName) {
        Tag toRemove = null;
        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            if (t.getName().equals(pName)) {
                toRemove = t;
                break;
            }
        }
        if (toRemove != null) {
            removeElement(toRemove);
        }
    }

    /**Add a tag to a village*/
    public synchronized void addTag(Village pVillage, String pTag, boolean pUpdate) {
        if (pTag == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            if (pVillage == null) {
                logger.debug("Adding tag '" + pTag + "'");
            } else {
                logger.debug("Adding tag '" + pTag + "' to village " + pVillage);
            }
        }
        boolean added = false;
        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            if (t.getName().equals(pTag)) {
                if (t instanceof LinkedTag) {
                    //tag exists as linked tag -> remove linked tag before
                    logger.debug("Linked tag with same name found. Removing linked tag '" + pTag + "'");
                    removeElement(t);
                    break;
                } else {
                    if (pVillage != null) {
                        t.tagVillage(pVillage.getId());
                    }
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding new tag " + pTag + ((pVillage != null) ? (" for village " + pVillage) : ""));
            }
            Tag nt = new Tag(pTag, true);
            if (pVillage != null) {
                //add only valid villages
                nt.tagVillage(pVillage.getId());
            }
            addManagedElement(nt);
        }
    }
    
    public String[] getAllTagNames() {
    	List<String> tags = new LinkedList<>();
    	
    	for (Tag e : TagUtils.getTags(Tag.CASE_INSENSITIVE_ORDER)) {
    		if(e.getVillageIDs().size() != 0)
    		tags.add(e.getName());
            }
    	    	
		return tags.toArray(new String[tags.size()]);
        }

    public synchronized void addTag(Village pVillage, String pTag) {
        addTag(pVillage, pTag, true);
    }

    /**Add a tag without villages*/
    public synchronized void addTag(String pTag) {
        addTag(null, pTag, true);
    }

    public synchronized void addTagFast(String pTag) {
        addTag(null, pTag, false);
    }

    /**Add a tag to a village*/
    public synchronized void addLinkedTag(LinkedTag pLinkedTag) {
        if (pLinkedTag == null) {
            return;
        }
        addManagedElement(pLinkedTag);
    }

    /**Remove a tag from a village*/
    public synchronized void removeTag(Village pVillage, String pTag) {
        if (pTag == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Removing tag '" + pTag + "' from village " + pVillage);
        }
        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            if (t.getName().equals(pTag)) {
                t.untagVillage(pVillage.getId());
            }
        }
    }

    /**Remove all tags from a village*/
    public synchronized void removeTags(Village pVillage) {
        if (pVillage == null) {
            return;
        }

        for (ManageableType e : getAllElements()) {
            Tag t = (Tag) e;
            t.untagVillage(pVillage.getId());
        }
    }

    public synchronized boolean shouldVillageBeRendered(Village pVillage) {
        Tag[] villageTags = getTags(pVillage).toArray(new Tag[]{});
        boolean drawVillage = true;
        if ((villageTags != null) && (villageTags.length != 0)) {
            boolean notShown = true;
            for (Tag tag : villageTags) {
                if (tag.isShowOnMap()) {
                    //at least one of the tags for the village is visible
                    notShown = false;
                    break;
                }
            }
            if (notShown) {
                drawVillage = false;
            }
        }
        return drawVillage;
    }
}
