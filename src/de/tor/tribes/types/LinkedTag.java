/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.types;

import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.tag.TagManager;
import java.net.URLDecoder;
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
     */
    public static LinkedTag fromXml(Element pElement) throws Exception {
        Tag t = Tag.fromXml(pElement);
        LinkedTag lt = new LinkedTag(t.getName(), t.isShowOnMap());
        lt.setMapMarker(t.getMapMarker());
        String equation = URLDecoder.decode(pElement.getChild("equation").getTextTrim(), "UTF-8");
        lt.setEquation(equation);
        return lt;
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
