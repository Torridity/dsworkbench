/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.tag;

import de.tor.tribes.types.Tag;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.VillageTagFrame;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import org.jdom.Document;
import org.jdom.Element;

/**Manager for village tags. Tags can be stored in files or in a database (not implemented yet)
 * @author Jejkal
 */
public class TagManager {

    private static Logger logger = Logger.getLogger("TagManager");
    private static TagManager SINGLETON = null;
    private final static List<Tag> mTags = new LinkedList<Tag>();
    private final List<TagManagerListener> mManagerListeners = new LinkedList<TagManagerListener>();

    public static synchronized TagManager getSingleton() {
        if (SINGLETON == null) {
            System.out.println("TagManager");
            SINGLETON = new TagManager();
        }
        return SINGLETON;
    }

    TagManager() {
    }

    public synchronized void addTagManagerListener(TagManagerListener pListener) {
        if (pListener == null) {
            return;
        }
        if (!mManagerListeners.contains(pListener)) {
            mManagerListeners.add(pListener);
        }
    }

    public synchronized void removeTagManagerListener(TagManagerListener pListener) {
        mManagerListeners.remove(pListener);
    }

    /**Load tags from a file*/
    public void loadTagsFromFile(String pFile) {
        mTags.clear();
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        File tagFile = new File(pFile);
        if (tagFile.exists()) {

            if (logger.isDebugEnabled()) {
                logger.debug("Loading tags from '" + pFile + "'");
            }
            try {
                Document d = JaxenUtils.getDocument(tagFile);
                int cnt = 0;
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//tags/tag")) {
                    try {
                        mTags.add(Tag.fromXml(e));
                    } catch (Exception inner) {
                        cnt++;
                    }
                }
                if(cnt == 0){
                    logger.debug("Tags loaded successfully");
                }else{
                    logger.warn(cnt + " errors while loading tags");
                }
            } catch (Exception e) {
                logger.error("Failed to load tags", e);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("No tags found under '" + pFile + "'");
            }
        }
        VillageTagFrame.getSingleton().updateUserTags();
    }

    /**Load tags from a database (not implemented yet*/
    public void loadFromDatabase(String pUrl) {
    }

    /**Save tags to a file*/
    public void saveTagsToFile(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
        }
        try {
            logger.debug("Writing tags to '" + pFile + "'");
            FileWriter w = new FileWriter(pFile);
            w.write("<tags>\n");
            for (Tag t : mTags) {
                w.write(t.toXml());
            }
            w.write("</tags>\n");
            w.flush();
            w.close();
            logger.debug("Tags successfully saved");
        } catch (Exception e) {
            logger.error("Failed to store tags", e);
        }
    }

    /**Save tags to a database (not implemented yet)*/
    public void saveTagsToDatabase(String pUrl) {
    }

    public synchronized List<Tag> getTags() {
        return mTags;
    }

    /**Get all tags for a village*/
    public synchronized List<Tag> getTags(Village pVillage) {
        if (pVillage == null) {
            return new LinkedList<Tag>();
        }
        List<Tag> tags = new LinkedList<Tag>();
        for (Tag t : mTags) {
            if (t.tagsVillage(pVillage.getId())) {
                tags.add(t);
            }
        }
        return tags;
    }

    public Tag getTagByName(String pName) {
        for (Tag t : mTags) {
            if (t.getName().equals(pName)) {
                return t;
            }
        }
        return null;
    }

    public void removeTagByName(String pName) {
        Tag toRemove = null;
        for (Tag t : mTags) {
            if (t.getName().equals(pName)) {
                toRemove = t;
                break;
            }
        }
        if (toRemove != null) {
            removeTag(toRemove);
        }
        fireTagsChangedEvents();
    }

    /**Add a tag to a village*/
    public synchronized void addTag(Village pVillage, String pTag) {
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
        for (Tag t : mTags) {
            if (t.getName().equals(pTag)) {
                if (pVillage != null) {
                    t.tagVillage(pVillage.getId());
                }
                added = true;
                break;
            }
        }

        if (!added) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding new tag " + pTag + " for village " + pVillage);
            }
            Tag nt = new Tag(pTag, true);
            if (pVillage != null) {
                //add only valid villages
                nt.tagVillage(pVillage.getId());
            }
            mTags.add(nt);
        }
        fireTagsChangedEvents();
    }

    /**Add a tag without villages*/
    public synchronized void addTag(String pTag) {
        addTag(null, pTag);
    }

    /**Remove a tag from a village*/
    public synchronized void removeTag(Village pVillage, String pTag) {

        if (pTag == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Removing tag '" + pTag + "' from village " + pVillage);
        }
        for (Tag t : mTags) {
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
        if (logger.isDebugEnabled()) {
            logger.debug("Removing all tags for village " + pVillage);
        }

        for (Tag t : mTags) {
            t.untagVillage(pVillage.getId());
        }
    }

    public synchronized void removeTag(Tag pTag) {
        mTags.remove(pTag);
        fireTagsChangedEvents();
    }

    /**Notify attack manager listeners about changes*/
    private void fireTagsChangedEvents() {
        TagManagerListener[] listeners = mManagerListeners.toArray(new TagManagerListener[]{});
        for (TagManagerListener listener : listeners) {
            listener.fireTagsChangedEvent();
        }
        VillageTagFrame.getSingleton().updateUserTags();
    }
}
