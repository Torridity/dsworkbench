/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import org.jdom.Element;
import de.tor.tribes.util.xml.JaxenUtils;
import java.awt.Color;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Document;

/**
 *
 * @author Charon
 */
public class Tag implements Comparable<Tag> {

    /**<tags>
     * <tag name="TagName" shownOnMap="true">
     * <village>4711</village>
     * <village>4712</village>
     * </tag>
     * </tags>
     * 
     */
    public static final Comparator<Tag> CASE_INSENSITIVE_ORDER = new CaseInsensitiveTagComparator();
    private String sName = null;
    private List<Integer> mVillageIDs = new LinkedList<Integer>();
    //private Color tagColor = null;
    //-1 means no icon
    private TagMapMarker mapMarker = null;//; int tagIcon = -1;
    private boolean showOnMap = true;

    public static Tag fromXml(Element pElement) throws Exception {
        String name = URLDecoder.decode(pElement.getChild("name").getTextTrim(), "UTF-8");
        boolean showOnMap = Boolean.parseBoolean(pElement.getAttributeValue("shownOnMap"));
        Tag t = new Tag(name, showOnMap);
        try {
            Element color = pElement.getChild("color");
            int r = color.getAttribute("r").getIntValue();
            int g = color.getAttribute("g").getIntValue();
            int b = color.getAttribute("b").getIntValue();
            t.setTagColor(new Color(r, g, b));
        } catch (Exception e) {
            t.setTagColor(null);
        }

        try {
            Element icon = pElement.getChild("icon");
            t.setTagIcon(Integer.parseInt(icon.getText()));
        } catch (Exception e) {
            t.setTagIcon(-1);
        }

        for (Element e : (List<Element>) JaxenUtils.getNodes(pElement, "villages/village")) {
            t.tagVillage(Integer.parseInt(e.getValue()));
        }
        return t;
    }

    public Tag(String pName, boolean pShowOnMap) {
        setName(pName);
        setShowOnMap(pShowOnMap);
        mapMarker = new TagMapMarker();
    }

    public String getName() {
        return sName;
    }

    public void setName(String pName) {
        this.sName = pName;
    }

    public TagMapMarker getMapMarker() {
        return mapMarker;
    }

    public void tagVillage(Integer pVillageID) {
        if (!mVillageIDs.contains(pVillageID)) {
            mVillageIDs.add(pVillageID);
        }
    }

    public void untagVillage(Integer pVillageID) {
        mVillageIDs.remove(pVillageID);
    }

    public List<Integer> getVillageIDs() {
        return mVillageIDs;
    }

    public boolean tagsVillage(int pVillageID) {
        return mVillageIDs.contains(pVillageID);
    }

    public void setShowOnMap(boolean pValue) {
        showOnMap = pValue;
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toXml() throws Exception {
        try {
            String ret = "<tag shownOnMap=\"" + isShowOnMap() + "\">\n";
            ret += "<name><![CDATA[" + URLEncoder.encode(getName(), "UTF-8") + "]]></name>\n";
            Color c = getTagColor();
            if (c != null) {
                ret += "<color r=\"" + c.getRed() + "\" g=\"" + c.getGreen() + "\" b=\"" + c.getBlue() + "\"/>\n";
            }
            ret += "<icon>" + getTagIcon() + "</icon>\n";
            ret += "<villages>\n";
            for (Integer i : mVillageIDs) {
                ret += "<village>" + i + "</village>\n";
            }
            ret += "</villages>\n";
            ret += "</tag>\n";
            return ret;
        } catch (Exception e) {
            return "\n";
        }
    }

    public static void main(String[] args) throws Exception {
        String tag = "<tags><tag shownOnMap=\"true\"><name><![CDATA[Mein Tag]]></name><villages><village>4711</village></villages></tag></tags>";
        Document d = JaxenUtils.getDocument(tag);
        for (Element e : (List<Element>) JaxenUtils.getNodes(d, "//tags/tag")) {
            System.out.println(Tag.fromXml(e));
        }
    }

    /**
     * @return the tagColor
     */
    public Color getTagColor() {
        return mapMarker.getTagColor();
    }

    /**
     * @param tagColor the tagColor to set
     */
    public void setTagColor(Color tagColor) {
        mapMarker.setTagColor(tagColor);
    }

    /**
     * @return the tagIcon
     */
    public int getTagIcon() {
        return mapMarker.getTagIcon();
    }

    /**
     * @param tagIcon the tagIcon to set
     */
    public void setTagIcon(int tagIcon) {
        mapMarker.setTagIcon(tagIcon);
    }

    private static class CaseInsensitiveTagComparator implements Comparator<Tag>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(Tag s1, Tag s2) {
            int n1 = s1.toString().length(), n2 = s2.toString().length();
            for (int i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
                char c1 = s1.toString().charAt(i1);
                char c2 = s2.toString().charAt(i2);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }
    }

    @Override
    public int compareTo(Tag o) {
        return CASE_INSENSITIVE_ORDER.compare(this, o);
    }
}
