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
package de.tor.tribes.util;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.ServerManager;
import de.tor.tribes.types.ext.Ally;
import de.tor.tribes.types.ext.InvalidTribe;
import de.tor.tribes.types.ext.Tribe;
import de.tor.tribes.types.ext.Village;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Torridity
 */
public class BBCodeFormatter {

    public static String toHtml(String pBBText) {
        //replace standard HTML items
        Map<String, String> bbMap = new HashMap<String, String>();
        bbMap.put("(\r\n|\r|\n|\n\r)", "<br/>");
        bbMap.put("\\[b\\](.+?)\\[/b\\]", "<strong>$1</strong>");
        //bbMap.put(Pattern.compile("\\[table\\](.+?)"), "<table border='0' cellspacing='1' cellpadding='4' class='vis' style='border:#999966 1px solid'> $1");
        bbMap.put("\\[table\\]", "<table border='0' cellspacing='1' cellpadding='4' class='vis' style='border:#999966 1px solid'>");
        //bbMap.put(Pattern.compile("(.+?)\\[/table\\]"), "$1</table>");
        bbMap.put("\\[/table\\]", "</table>");
        //bbMap.put(Pattern.compile("(.+?)\\[\\*\\*\\](.+?)"), "$1<tr><th>$2");
        bbMap.put("\\[\\*\\*\\]", "<tr><th>");
        //bbMap.put(Pattern.compile("(.+?)\\[/\\*\\*\\](.+?)"), "$1</th></tr>$2");
        bbMap.put("\\[/\\*\\*\\]", "</th></tr>");
        bbMap.put("\\[\\|\\|\\]", "</th><th>");
        //bbMap.put(Pattern.compile("(.+?)\\[\\*\\](.+?)"), "$1<tr><td>$2");
        bbMap.put("\\[\\*\\]", "<tr><td>");
        //bbMap.put(Pattern.compile("(.+?)\\[/\\*\\](.+?)"), "$1</td></tr>$2");
        bbMap.put("\\[/\\*\\]", "</td></tr>");
        bbMap.put("\\[\\|\\]", "</td><td>");
        bbMap.put("\\[s\\](.+?)\\[/s\\]", "<s>$1</s>");
        bbMap.put("\\[i\\](.+?)\\[/i\\]", "<span style='font-style:italic;'>$1</span>");
        bbMap.put("\\[u\\](.+?)\\[/u\\]", "<span style='text-decoration:underline;'>$1</span>");
        bbMap.put("\\[quote\\](.+?)\\[/quote\\]", "<div style='display: block;font-size: 12pt;padding-bottom: 0px;padding-left: 10px;padding-right: 0px;padding-top: 0px;background-color:#FFFFFF;'>$1</div>");
        bbMap.put("\\[quote=(.+?)\\](.+?)\\[/quote\\]", "<div style='display: block;font-size: 12pt;padding-bottom: 0px;padding-left: 10px;padding-right: 0px;padding-top: 5px;'><b>$1 hat geschrieben:</b><div style='background-color:#FFFFFF;'>$2</div></div>");
        bbMap.put("\\[color=(.+?)\\](.+?)\\[/color\\]", "<span style='color:$1;'>$2</span>");
        bbMap.put("\\[size=(.+?)\\](.+?)\\[/size\\]", "<span style='font-size:$1;'>$2</span>");
        bbMap.put("\\[img\\](.+?)\\[/img\\]", "<img src='$1'/>");
        bbMap.put("\\[url\\](.+?)\\[/url\\]", "<a href='$1'>$1</a>");
        bbMap.put("\\[url=(.+?)\\](.+?)\\[/url\\]", "<a href='$1'>$2</a>");
        String url = ServerManager.getServerURL(GlobalOptions.getSelectedServer());
        bbMap.put("\\[unit\\](.+?)\\[/unit\\]", "<img src='" + url + "/graphic/unit/unit_$1.png'/>");
        String html = pBBText;
        int lBefore = 0;
        do {
            //do several times to get wrapped contents
            lBefore = html.length();
            for (Map.Entry entry : bbMap.entrySet()) {
                html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
            }
        } while (html.length() != lBefore);

        bbMap.clear();

        //replace special items
        bbMap.put("\\[tribe\\](.+?)\\[/tribe\\]", "$1");
        bbMap.put("\\[player\\](.+?)\\[/player\\]", "$1");
        bbMap.put("\\[ally\\](.+?)\\[/ally\\]", "$1");
        bbMap.put("\\[coord\\](.+?)\\[/coord\\]", "$1");
        bbMap.put("\\[village\\](.+?)\\[/village\\]", "$1");

        do {
            //do several times to get wrapped contents
            lBefore = html.length();
            for (Map.Entry entry : bbMap.entrySet()) {
                String key = entry.getKey().toString();
                Pattern p = Pattern.compile(key);
                Matcher m = p.matcher(html);
                
                //replace special items by links
                while (m.find()) {
                    String newValue = null;
                    if (key.indexOf("tribe") > -1 || key.indexOf("player") > -1) {
                        String tribe = html.substring(m.start(), m.end()).replaceAll(entry.getKey().toString(), "$1");
                        Tribe t = null;
                        try {
                            t = DataHolder.getSingleton().getTribeByName(tribe);
                            if (t.equals(InvalidTribe.getSingleton())) {
                                t = null;
                            }
                        } catch (Exception e) {
                        }
                        if (t != null) {
                            newValue = "<a href='#" + tribe + "' class='ds_link'>" + tribe + "</a>";
                        } else {
                            newValue = "<a href='#' class='ds_link'>Ung&uuml;ltiger Spieler</a>";
                        }
                    } else if (key.indexOf("ally") > -1) {
                        String ally = html.substring(m.start(), m.end()).replaceAll(entry.getKey().toString(), "$1");
                        Ally a = null;
                        try {
                            a = DataHolder.getSingleton().getAllyByTagName(ally);
                        } catch (Exception e) {
                        }
                        if (a != null) {
                            newValue = "<a href='##" + ally + "' class='ds_link'>" + ally + "</a>";
                        } else {
                            newValue = "<a href='##' class='ds_link'>Ung&uuml;ltiger Stamm</a>";
                        }
                    } else if (key.indexOf("coord") > -1 || key.indexOf("village") > -1) {
                        String coord = html.substring(m.start(), m.end()).replaceAll(key, "$1");
                        Village v = null;
                        try {
                            v = PluginManager.getSingleton().executeVillageParser(coord).get(0);
                        } catch (Exception e) {
                        }

                        if (v != null) {
                            newValue = "<a href='###" + coord + "' class='ds_link'>" + v.getFullName() + "</a>";
                        } else {
                            newValue = "<a href='###' class='ds_link'>Ung&uuml;ltiges Dorf</a>";
                        }
                    }
                    if (newValue != null) {
                        html = html.replaceFirst(key, EscapeChars.forRegex(newValue));
                        m = p.matcher(html);
                    }
                }
            }
        } while (html.length() != lBefore);
        return html;
    }

    public static String getStyles() {
        return "<style type='text/css'>"
                + ".ds_link{ color:#804000;font-weight:700;text-decoration:none}"
                + "a {color:#603000;text-decoration:none}"
                + "blockquote {background-color:#FFFFFF;}"
                + ".vis td { background:#f4e4bc; }"
                + ".vis th { font-size: 11pt; text-align: left; font-weight:700; background-color: #c1a264}"
                + "</style>";
    }
}
