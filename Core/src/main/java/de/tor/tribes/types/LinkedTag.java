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

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.types.ext.Village;
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
import org.jdom2.Element;

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
            sEquation = URLDecoder.decode(pElement.getChild("equation").getTextTrim(), "UTF-8");
        } catch (Exception ignored) {
        }
    }

    @Override
    public Element toXml(String elementName) {
        Element tag = new Element(elementName);
        
        try {
            tag.setAttribute("shownOnMap", Boolean.toString(isShowOnMap()));
            tag.addContent(new Element("name").setText(URLEncoder.encode(getName(), "UTF-8")));
            Color c = getTagColor();
            if (c != null) {
                Element color = new Element("color");
                color.setAttribute("r", Integer.toString(c.getRed()));
                color.setAttribute("g", Integer.toString(c.getGreen()));
                color.setAttribute("b", Integer.toString(c.getBlue()));
                tag.addContent(color);
            }
            tag.addContent(new Element("icon").setText(Integer.toString(getTagIcon())));
            tag.addContent(new Element("villages"));
            tag.addContent(new Element("equation").setText(URLEncoder.encode(sEquation, "UTF-8")));
        } catch (Exception e) {
        }
        return tag;
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
        //TODO find a better way for that (maybe whith implementation of real Dynamic groups)
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
