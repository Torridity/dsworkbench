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
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
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
    public void loadElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }
        File noteFile = new File(pFile);
        invalidate();
        initialize();
        if (noteFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading notes from '" + pFile + "'");
            }
            try {
                Document d = JDomUtils.getDocument(noteFile);
                List<Element> data = (List<Element>) JDomUtils.getNodes(d, "noteData");
                if (data == null || data.isEmpty()) {
                    logger.info("Loading legacy data format");
                    //old version
                    for (Element e1 : (List<Element>) JDomUtils.getNodes(d, "notes/note")) {
                        try {
                            Note n = new Note();
                            n.loadFromXml(e1);
                            addManagedElement(n);
                        } catch (Exception inner) {
                            //ignored, note invalid
                        }
                    }
                } else {
                    Element dataNode = data.get(0);
                    double version = Double.parseDouble(dataNode.getAttributeValue("version"));
                    for (Element e : (List<Element>) JDomUtils.getNodes(dataNode, "noteSets/noteSet")) {
                        String setKey = e.getAttributeValue("name");
                        setKey = URLDecoder.decode(setKey, "UTF-8");
                        if (logger.isDebugEnabled()) {
                            logger.debug("Loading note set '" + setKey + "'");
                        }
                        addGroup(setKey);
                        for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "notes/note")) {
                            try {
                                Note n = new Note();
                                n.loadFromXml(e1);
                                addManagedElement(setKey, n);
                            } catch (Exception inner) {
                                //ignored, marker invalid
                            }
                        }
                    }
                }
                logger.debug("Notes successfully loaded");
            } catch (Exception e) {
                logger.error("Failed to load notes", e);
            }

        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Notes file not found under '" + pFile + "'");
            }
        }
        revalidate();
    }

    @Override
    public void saveElements(String pFile) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Writing notes to '" + pFile + "'");
        }
        try {
            StringBuilder b = new StringBuilder();
            b.append("<data><noteData version=\"1.0\">\n");
            b.append("<noteSets>\n");
            Iterator<String> plans = getGroupIterator();

            while (plans.hasNext()) {
                String key = plans.next();
                b.append("<noteSet name=\"").append(URLEncoder.encode(key, "UTF-8")).append("\">\n");
                List<ManageableType> elems = getAllElements(key);
                b.append("<notes>\n");
                for (ManageableType elem : elems) {
                    b.append(elem.toXml()).append("\n");
                }

                b.append("</notes>\n");
                b.append("</noteSet>\n");
            }
            b.append("</noteSets>");
            b.append("</noteData></data>");
            FileWriter w = new FileWriter(pFile);
            w.write(b.toString());
            w.flush();
            w.close();
            logger.debug("Notes successfully saved");
        } catch (Exception e) {
            if (!new File(pFile).getParentFile().exists()) {
                //server directory obviously does not exist yet
                //this should only happen at the first start
                logger.info("Ignoring error, server directory does not exists yet");
            } else {
                logger.error("Failed to save notes", e);
            }
        }
    }

    @Override
    public String getExportData(List<String> pGroupsToExport) {
        if (pGroupsToExport.isEmpty()) {
            return "";
        }
        logger.debug("Generating notes export data");
        StringBuilder b = new StringBuilder();
        b.append("<noteData>\n");
        b.append("<noteSets>\n");

        for (String set : pGroupsToExport) {
            try {
                b.append("<noteSet name=\"").append(URLEncoder.encode(set, "UTF-8")).append("\">\n");
                ManageableType[] elements = getAllElements(set).toArray(new ManageableType[getAllElements(set).size()]);
                b.append("<notes>\n");

                for (ManageableType elem : elements) {
                    b.append(elem.toXml()).append("\n");
                }
                b.append("</notes>\n");
                b.append("</noteSet>\n");
            } catch (Exception e) {
                logger.warn("Failed to export note set'" + set + "'", e);
            }
        }
        b.append("</noteSets>\n");
        b.append("</noteData>\n");
        logger.debug("Export data generated successfully");
        return b.toString();
    }

    @Override
    public boolean importData(File pFile, String pExtension) {
        if (pFile == null) {
            logger.error("File argument is 'null'");
            return false;
        }
        invalidate();
        boolean result = false;
        logger.info("Loading notes");
        try {
            Document d = JDomUtils.getDocument(pFile);

            List<Element> data = (List<Element>) JDomUtils.getNodes(d, "noteData");
            if (data == null || data.isEmpty()) {
                for (Element e : (List<Element>) JDomUtils.getNodes(d, "notes/note")) {
                    Note note = new Note();
                    note.loadFromXml(e);
                    if (note != null) {
                        addManagedElement(note);
                    }
                }
            } else {
                Element dataNode = data.get(0);
                for (Element e : (List<Element>) JDomUtils.getNodes(dataNode, "noteSets/noteSet")) {
                    String setKey = e.getAttributeValue("name");
                    setKey = URLDecoder.decode(setKey, "UTF-8");
                    if (pExtension != null) {
                        setKey += "_" + pExtension;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading note set '" + setKey + "'");
                    }
                    addGroup(setKey);

                    for (Element e1 : (List<Element>) JDomUtils.getNodes(e, "notes/note")) {
                        try {
                            Note n = new Note();
                            n.loadFromXml(e1);
                            addManagedElement(setKey, n);
                        } catch (Exception inner) {
                            //ignored, marker invalid
                        }
                    }
                }
            }
            logger.debug("Notes imported successfully");
            result = true;
        } catch (Exception e) {
            logger.error("Failed to import notes", e);
            DSWorkbenchNotepad.getSingleton().resetView();
        }
        revalidate(true);
        return result;
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

    public Hashtable<Village, List<Note>> getNotesMap() {
        Hashtable<Village, List<Note>> noteMap = new Hashtable<>();
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
