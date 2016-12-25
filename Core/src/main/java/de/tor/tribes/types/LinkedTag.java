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
package de.tor.tribes.types;

import de.tor.tribes.types.ext.Village;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.tag.TagManager;
import java.awt.Color;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.jdom.Element;

/**
 *
 * @author Torridity
 */
public class LinkedTag extends Tag {

    private String sEquation = null;

    @Override
    public void loadFromXml(Element pElement) {
        try {
            String name = URLDecoder.decode(pElement.getChild("name").getTextTrim(), "UTF-8");
            boolean bShowOnMap = Boolean.parseBoolean(pElement.getAttributeValue("shownOnMap"));
            setName(name);
            setShowOnMap(bShowOnMap);
            try {
                Element color = pElement.getChild("color");
                int r = color.getAttribute("r").getIntValue();
                int g = color.getAttribute("g").getIntValue();
                int b = color.getAttribute("b").getIntValue();
                setTagColor(new Color(r, g, b));
            } catch (Exception e) {
                setTagColor(null);
            }

            try {
                Element icon = pElement.getChild("icon");
                setTagIcon(Integer.parseInt(icon.getText()));
            } catch (Exception e) {
                setTagIcon(-1);
            }
            String equation = URLDecoder.decode(pElement.getChild("equation").getTextTrim(), "UTF-8");
            sEquation = equation;
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toXml() {
        try {
            StringBuilder b = new StringBuilder();
            b.append("<tag shownOnMap=\"").append(isShowOnMap()).append("\">\n");
            b.append("<name><![CDATA[").append(URLEncoder.encode(getName(), "UTF-8")).append("]]></name>\n");
            Color c = getTagColor();
            if (c != null) {
                b.append("<color r=\"").append(c.getRed()).append("\" g=\"").append(c.getGreen()).append("\" b=\"").append(c.getBlue()).append("\"/>\n");
            }
            b.append("<icon>").append(getTagIcon()).append("</icon>\n");
            b.append("<villages/>\n");
            b.append("<equation>\n");
            b.append(URLEncoder.encode(sEquation, "UTF-8")).append("\n");
            b.append("</equation>\n");
            b.append("</tag>\n");
            return b.toString();
        } catch (Exception e) {
            return "\n";
        }
    }

    public LinkedTag() {
        super();
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
        List<ManageableType> elements = TagManager.getSingleton().getAllElements();
        List<Tag> lTags = new ArrayList<>();
        for (ManageableType t : elements) {
            lTags.add((Tag) t);
        }
        Collections.sort(lTags, Tag.SIZE_ORDER);
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        for (Village v : GlobalOptions.getSelectedProfile().getTribe().getVillageList()) {
            String equation = sEquation;
            for (Tag t : lTags) {
                equation = equation.replaceAll(Matcher.quoteReplacement(t.getName()), Boolean.toString(t.tagsVillage(v.getId())));
            }

            equation = equation.replaceAll(("K" + ((v.getContinent() < 10) ? "0" : "") + v.getContinent()), "true");
            try {
                engine.eval("var b = eval(\"" + equation + "\")");
                Boolean b = (Boolean) engine.get("b");
                if (b) {
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
