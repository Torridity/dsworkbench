/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.tag;

import de.tor.tribes.io.DataHolder;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import org.jdom.Document;
import org.jdom.Element;

/**Manager for village tags. Tags can be stored in files or in a database (not implemented yet)
 * @author Jejkal
 */
public class TagManager {

    private static Logger logger = Logger.getLogger(TagManager.class);
    private static TagManager SINGLETON = null;
    private static Hashtable<Village, List<String>> mTags = null;

    public static synchronized TagManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TagManager();
        }
        return SINGLETON;
    }

    TagManager() {
        mTags = new Hashtable<Village, List<String>>();
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
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//villageTags/villageTagList")) {
                    try {
                        Integer id = Integer.parseInt(JaxenUtils.getNodeValue(e, "id"));
                        Village v = GlobalOptions.getDataHolder().getVillagesById().get(id);
                        if (v != null) {
                            List<Element> tags = (List<Element>) JaxenUtils.getNodes(e, "tags/tag");
                            List<String> tagList = new LinkedList<String>();
                            for (Element tag : tags) {
                                tagList.add(tag.getText());
                            }
                            mTags.put(v, tagList);
                        }
                    } catch (Exception inner) {
                    }
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


        logger.debug("Associating tags with villages");
        //set the tags for the tagged villages
        Enumeration<Village> keys = mTags.keys();
        while (keys.hasMoreElements()) {
            Village next = keys.nextElement();
            List<String> tags = mTags.remove(next);
            mTags.put(DataHolder.getSingleton().getVillages()[next.getX()][next.getY()], tags);
        }
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
            w.write("<villageTags>\n");
            Enumeration<Village> e = mTags.keys();
            while (e.hasMoreElements()) {
                Village current = e.nextElement();
                if (current != null) {
                    w.write("<villageTagList>\n");
                    w.write("<id>" + current.getId() + "</id>\n");
                    List<String> tags = mTags.get(current);
                    if (tags.size() > 0) {
                        w.write("<tags>\n");
                        for (String tag : tags) {
                            w.write("<tag>" + tag + "</tag>\n");
                        }
                        w.write("</tags>\n");
                    }
                    w.write("</villageTagList>\n");
                }
            }
            w.write("</villageTags>\n");
            w.flush();
            w.close();
            logger.debug("Tags successfully saved");
        } catch (Exception e) {
            logger.error("Failed to store tags", e);
        }
    }

    /**Save tags to a database (not implemented yet)*/
    public  void saveTagsToDatabase(String pUrl) {
    }

    /**Get all tags for a village*/
    public synchronized List<String> getTags(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        return mTags.get(pVillage);
    }

    /**Add a tag to a village*/
    public synchronized void addTag(Village pVillage, String pTag) {
        List<String> tags = mTags.get(pVillage);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding tag '" + pTag + "' to village " + pVillage);
        }

        if (tags == null) {
            tags = new LinkedList<String>();
            tags.add(pTag);
            mTags.put(pVillage, tags);
        } else {
            tags.add(pTag);
        }
    }

    /**Remove a tag from a village*/
    public synchronized void removeTag(Village pVillage, String pTag) {
        List<String> tags = mTags.get(pVillage);
        if (tags == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Removing tag '" + pTag + "' from village " + pVillage);
        }
        tags.remove(pTag);
        if (tags.isEmpty()) {
            mTags.remove(pVillage);
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
        mTags.remove(pVillage);
    }
}
