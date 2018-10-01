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
import de.tor.tribes.control.GenericManagerListener;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.LinkedTag;
import de.tor.tribes.types.Tag;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.TagUtils;
import de.tor.tribes.util.xml.JDomUtils;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**Manager for village tags. Tags can be stored in files or in a database (not implemented yet)
 * @author Torridity
 */
public class TagManager extends GenericManager<Tag> {

    private static Logger logger = LogManager.getLogger("TagManager");
    private static TagManager SINGLETON = null;

    public static synchronized TagManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TagManager();
        }
        return SINGLETON;
    }

    TagManager() {
        super(false);
        
        this.addManagerListener(new GenericManagerListener() {
            @Override
            public void dataChangedEvent() {
                updateLinkedTags();
            }

            @Override
            public void dataChangedEvent(String pGroup) {
                dataChangedEvent();
            }
        });
    }

    @Override
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        invalidate();
        logger.debug("Loading tags");
        
        try {
            for (Element e : JDomUtils.getNodes(pElm, "tags/tag")) {
                Tag t = null;
                if (e.getChild("equation") != null) {
                    t = new LinkedTag();
                    t.loadFromXml(e);
                } else {
                    t = new Tag();
                    t.loadFromXml(e);
                }
                addManagedElement(t);
                result++;
            }
            logger.debug("Tags loaded successfully");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to load tags", e);
        }
        revalidate(true);
        
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element tags = new Element("tags");
        
        try {
            logger.debug("Generating tag data");

            for (ManageableType e : getAllElements()) {
                tags.addContent(e.toXml("tag"));
            }
            logger.debug("Data generated successfully");
        } catch (Exception e) {
            logger.error("Failed to generate export data for tags", e);
        }
        return tags;
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
