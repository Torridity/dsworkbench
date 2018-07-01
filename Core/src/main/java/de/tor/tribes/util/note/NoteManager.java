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
package de.tor.tribes.util.note;

import de.tor.tribes.control.GenericManager;
import de.tor.tribes.control.ManageableType;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Note;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.views.DSWorkbenchNotepad;
import de.tor.tribes.util.xml.JDomUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class NoteManager extends GenericManager<Note> {

    private static Logger logger = LogManager.getLogger("NoteManager");
    private static NoteManager SINGLETON = null;

    public static synchronized NoteManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new NoteManager();
        }
        return SINGLETON;
    }

    NoteManager() {
        super(true);
    }

    @Override
    public String[] getGroups() {
        String[] groups = super.getGroups();
        Arrays.sort(groups, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if (o1.equals(DEFAULT_GROUP)) {
                    return -1;
                } else if (o2.equals(DEFAULT_GROUP)) {
                    return 1;
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
                }
            }
        });
        return groups;
    }

    @Override
    public int importData(Element pElm, String pExtension) {
        if (pElm == null) {
            logger.error("Element argument is 'null'");
            return -1;
        }
        int result = 0;
        invalidate();
        logger.info("Loading notes");
        try {
            List<Element> data = (List<Element>) JDomUtils.getNodes(pElm, "noteData");
            if (data == null || data.isEmpty()) {
                logger.debug("No data element found");
                return -1;
            }
            
            Element dataNode = data.get(0);
            try {
                double version = Double.parseDouble(dataNode.getAttributeValue("version"));
            } catch(Exception ignored) {};
            
            for (Element e : (List<Element>) JDomUtils.getNodes(dataNode, "noteSets/noteSet")) {
                String setKey = e.getAttributeValue("name");
                setKey = URLDecoder.decode(setKey, "UTF-8");
                if (pExtension != null) {
                    setKey += "_" + pExtension;
                }
                logger.debug("Loading note set '{}'", setKey);
                addGroup(setKey);

                for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "notes/note")) {
                    try {
                        Note n = new Note();
                        n.loadFromXml(e1);
                        addManagedElement(setKey, n);
                        result++;
                    } catch (Exception inner) {
                        //ignored, marker invalid
                    }
                }
            }
            logger.debug("Notes imported successfully");
        } catch (Exception e) {
            result = result * (-1) - 1;
            logger.error("Failed to import notes", e);
            DSWorkbenchNotepad.getSingleton().resetView();
        }
        revalidate(true);
        return result;
    }

    @Override
    public Element getExportData(final List<String> pGroupsToExport) {
        Element noteData = new Element("noteData");
        if (pGroupsToExport == null || pGroupsToExport.isEmpty()) {
            return noteData;
        }
        logger.debug("Generating notes data");
        noteData.setAttribute("version", "1.0");
        Element noteSets = new Element("noteSets");
        for (String set : pGroupsToExport) {
            try {
                Element noteSet = new Element("noteSet");
                noteSet.setAttribute("name", URLEncoder.encode(set, "UTF-8"));
                
                Element notes = new Element("notes");
                for (ManageableType elem : getAllElements(set)) {
                    notes.addContent(elem.toXml("note"));
                }
                noteSet.addContent(notes);
                noteSets.addContent(noteSet);
            } catch (Exception e) {
                logger.warn("Failed to generate note set'" + set + "'", e);
            }
        }
        noteData.addContent(noteSets);
        
        logger.debug("Data generated successfully");
        return noteData;
    }

    public Note getNoteForVillage(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        for (ManageableType t : getAllElementsFromAllGroups()) {
            Note n = (Note) t;
            if (n.getVillageIds().contains(pVillage.getId())) {
                return n;
            }
        }
        return null;
    }

    public List<Note> getNotesForVillage(Village pVillage) {
        if (pVillage == null) {
            return null;
        }
        List<Note> noteList = new LinkedList<>();
        for (ManageableType t : getAllElementsFromAllGroups()) {
            Note n = (Note) t;
            if (n.getVillageIds().contains(pVillage.getId())) {
                noteList.add(n);
            }
        }
        return noteList;
    }

    public HashMap<Village, List<Note>> getNotesMap() {
        HashMap<Village, List<Note>> noteMap = new HashMap<>();
        for (ManageableType t : getAllElementsFromAllGroups()) {
            Note n = (Note) t;
            for (Integer id : n.getVillageIds()) {
                Village v = DataHolder.getSingleton().getVillagesById().get(id);
                if (v != null) {
                    List<Note> list = noteMap.get(v);
                    if (list == null) {
                        list = new ArrayList<>();
                        noteMap.put(v, list);
                    }
                    list.add(n);
                }
            }
        }
        return noteMap;
    }
}
