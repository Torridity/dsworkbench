/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.tag;

import de.tor.tribes.types.Tag;
import de.tor.tribes.io.DataHolder;
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import de.tor.tribes.types.Village;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedList;
import org.jdom.Document;
import org.jdom.Element;

/**Manager for village tags. Tags can be stored in files or in a database (not implemented yet)
 * @author Jejkal
 *  @TODO: graphics/icons/build.png and troops.png to Release!!
 */
public class TagManager {

    private static Logger logger = Logger.getLogger(TagManager.class);
    private static TagManager SINGLETON = null;
    private static Hashtable<Village, List<String>> mVillageTags = null;
    private static List<Tag> mTags = null;

    public static synchronized TagManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TagManager();
        }
        return SINGLETON;
    }

    TagManager() {
        mVillageTags = new Hashtable<Village, List<String>>();
        loadUserTags();
    }

    /**Load tags from a file*/
    public void loadTagsFromFile(String pFile) {
        mVillageTags.clear();
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
                        Village v = DataHolder.getSingleton().getVillagesById().get(id);
                        if (v != null) {
                            List<Element> tags = (List<Element>) JaxenUtils.getNodes(e, "tags/tag");
                            List<String> tagList = new LinkedList<String>();
                            for (Element tag : tags) {
                                String t = URLDecoder.decode(tag.getText(), "UTF-8");
                                if ((t != null) && (getUserTag(t) != null)) {
                                    //tag is valid
                                    tagList.add(t);
                                }
                            }
                            mVillageTags.put(v, tagList);
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
        Enumeration<Village> keys = mVillageTags.keys();
        while (keys.hasMoreElements()) {
            Village next = keys.nextElement();
            List<String> tags = mVillageTags.remove(next);
            mVillageTags.put(DataHolder.getSingleton().getVillages()[next.getX()][next.getY()], tags);
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
            Enumeration<Village> e = mVillageTags.keys();
            while (e.hasMoreElements()) {
                //walk all villag tag lists
                Village current = e.nextElement();
                boolean haveTag = false;
                if (current != null) {
                    String villageTagList = "<villageTagList>\n";
                    villageTagList += "<id>" + current.getId() + "</id>\n";
                    List<String> tags = mVillageTags.get(current);
                    //walk tags
                    if (tags.size() > 0) {
                        String villageTags = "<tags>\n";
                        for (String tag : tags) {
                            if (getUserTag(tag) != null) {
                                //tag is valid, so add it to the prepared xml
                                villageTags += "<tag>" + URLEncoder.encode(tag, "UTF-8") + "</tag>\n";
                                haveTag = true;
                            }
                        }
                        if (haveTag) {
                            //at least one tag was still valid, so add the village information to the xml
                            villageTags += "</tags>\n";
                            villageTagList += villageTags;
                        }
                    }
                    if (haveTag) {
                        //close the xml structure and write it to disk
                        villageTagList += "</villageTagList>\n";
                        w.write(villageTagList);
                    }
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
    public void saveTagsToDatabase(String pUrl) {
    }

    /**Get all tags for a village*/
    public synchronized List<String> getTags(Village pVillage) {
        if (pVillage == null) {
            return new LinkedList<String>();
        }
        List<String> tags = mVillageTags.get(pVillage);

        if (tags == null) {
            return new LinkedList<String>();
        }
        return tags;
    }

    /**Add a tag to a village*/
    public synchronized void addTag(Village pVillage, String pTag) {
        List<String> tags = mVillageTags.get(pVillage);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding tag '" + pTag + "' to village " + pVillage);
        }

        if (tags == null) {
            tags = new LinkedList<String>();
            tags.add(pTag);
            mVillageTags.put(pVillage, tags);
        } else {
            tags.add(pTag);
        }
    }

    /**Remove a tag from a village*/
    public synchronized void removeTag(Village pVillage, String pTag) {
        List<String> tags = mVillageTags.get(pVillage);
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

    public Tag getUserTag(String pName) {
        Tag[] tags = mTags.toArray(new Tag[]{});
        for (Tag t : tags) {
            if (t.getName().equals(pName)) {
                return t;
            }
        }
        return null;
    }

    public List<Tag> getUserTags() {
        return mTags;
    }

    //public synchronized void addUserTag(String pTag, String pResourcePath) {
    public synchronized void addUserTag(String pTag) {
        if (pTag == null) {
            //null tag not supported
            return;
        }
        if (getUserTag(pTag) == null) {
            //add only if it not exists yet
            //mTags.add(new Tag(pTag, pResourcePath, true));
            mTags.add(new Tag(pTag, true));
        }
    }

    public synchronized void removeUserTag(String pTag) {
        Tag[] tags = mTags.toArray(new Tag[]{});
        for (Tag t : tags) {
            if (t.getName().equals(pTag)) {
                mTags.remove(t);
                break;
            }
        }
    }

    /* public Image getUserTagIcon(String pTag) {
    for (Tag t : mTags) {
    if (t.getName().equals(pTag)) {
    return t.getTagIcon();
    }
    }
    return null;
    }
    
    public void setUserTagIcon(String pTag, String pIconPath) {
    for (Tag t : mTags) {
    if (t.getName().equals(pTag)) {
    t.setIconPath(pIconPath);
    break;
    }
    }
    }
    
    public void removeUserTagIcon(String pTag) {
    for (Tag t : mTags) {
    if (t.getName().equals(pTag)) {
    t.setIconPath(null);
    break;
    }
    }
    }
     */
    public void saveUserTags() {
        try {
            FileWriter w = new FileWriter("user_tags.xml");
            w.write("<tags>\n");
            for (Tag t : mTags) {
                w.write(t.toXml());
            }
            w.write("</tags>\n");
            w.flush();
            w.close();
        } catch (Exception e) {
            logger.error("Failed to write  user tags", e);
        }
    }

    public void loadUserTags() {
        mTags = new LinkedList<Tag>();
        File tagFile = new File("user_tags.xml");
        if (!tagFile.exists()) {
            /*mTags.add(new Tag("Off", "graphics/icons/axe.png", true));
            mTags.add(new Tag("Def", "graphics/icons/sword.png", true));
            mTags.add(new Tag("AG", "graphics/icons/snob.png", true));
            mTags.add(new Tag("Aufbau", "graphics/icons/build.png", true));
            mTags.add(new Tag("Truppenaufbau", "graphics/icons/troops.png", true));
            mTags.add(new Tag("Fertig", "graphics/icons/att.png", true));*/
            mTags.add(new Tag("Off", true));
            mTags.add(new Tag("Def", true));
            mTags.add(new Tag("AG", true));
            mTags.add(new Tag("Aufbau", true));
            mTags.add(new Tag("Truppenaufbau", true));
            mTags.add(new Tag("Fertig", true));
        } else {
            //try loading tags from file
            try {
                Document d = JaxenUtils.getDocument(tagFile);
                for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//tags/tag")) {
                    try {
                        mTags.add(Tag.fromXml(e));
                    } catch (Exception inner) {
                        //failed loading one tag
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to read user tags", e);
            }
        }
    }
}
