/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Color;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.jdom.Element;

/**
 *
 * @author Jejkal
 */
public class LinkedTag extends Tag {

    private String sEquation = null;

    /**Factor a linked tag from its DOM representation
     * @param pElement DOM element received while reading the user tags
     * @return Tag Tag instance parsed from pElement
     * @throws Exception
     */
    public static LinkedTag fromXml(Element pElement) throws Exception {
        String name = URLDecoder.decode(pElement.getChild("name").getTextTrim(), "UTF-8");
        boolean showOnMap = Boolean.parseBoolean(pElement.getAttributeValue("shownOnMap"));
        LinkedTag t = new LinkedTag(name, showOnMap);
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
        String equation = URLDecoder.decode(pElement.getChild("equation").getTextTrim(), "UTF-8");
        t.setEquation(equation);
        return t;
    }

    @Override
    public String toXml() throws Exception {
        try {
            String ret = "<tag shownOnMap=\"" + isShowOnMap() + "\">\n";
            ret += "<name><![CDATA[" + URLEncoder.encode(getName(), "UTF-8") + "]]></name>\n";
            Color c = getTagColor();
            if (c != null) {
                ret += "<color r=\"" + c.getRed() + "\" g=\"" + c.getGreen() + "\" b=\"" + c.getBlue() + "\"/>\n";
            }
            ret += "<icon>" + getTagIcon() + "</icon>\n";
            ret += "<villages/>\n";
            ret += "<equation>\n";
            ret += URLEncoder.encode(getEquation(), "UTF-8") + "\n";
            ret += "</equation>\n";
            ret += "</tag>\n";
            return ret;
        } catch (Exception e) {
            return "\n";
        }
    }

    public LinkedTag(String pName, boolean pShowOnMap) {
        super(pName, pShowOnMap);
    }

    public void setEquation(String pEquation) {
        sEquation = pEquation;
    }

    public String getEquation() {
        return sEquation;
    }

    @Override
    public void untagVillage(Integer pVillageID) {
        //not allowed
    }

    public void updateVillageList() {
        clearTaggedVillages();
        Tag[] tags = TagManager.getSingleton().getTags().toArray(new Tag[]{});
        Arrays.sort(tags, Tag.SIZE_ORDER);
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            String equation = sEquation;
            for (Tag t : tags) {
                equation = equation.replaceAll(Pattern.quote(t.getName()), Boolean.toString(t.tagsVillage(v.getId())));
            }

            equation = equation.replaceAll(Pattern.quote("K" + ((v.getContinent() < 10) ? "0" : "") + v.getContinent()), "true");
            try {
                engine.eval("var b = eval(\"" + equation + "\")");
                Boolean b = (Boolean) engine.get("b");
                if (b.booleanValue()) {
                    tagVillage(v.getId());
                }
            } catch (Exception e) {
                //error
            }
        }
    }

    public static void main(String[] args) {
        LinkedTag tag = new LinkedTag("test", true);
        tag.updateVillageList();

    }
}
